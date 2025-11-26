package Ultis

import Connector.MongoDB.MongoDBConnector
import Connector.Mysql.Repository.UserRepositoryMySQLImpl
import Connector.MongoDB.Repository.UserRepositoryMongoImpl
import Connector.Mysql.Service.UserService
import config.database.DatabaseConfig
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

import scala.concurrent.Await

class SparkWriteData(sparkSession: SparkSession, config: DatabaseConfig) {

  private val logger = LoggerFactory.getLogger(getClass)
  private val userRepositoryMySQL: UserRepositoryMySQLImpl = new UserRepositoryMySQLImpl
  private val userService: UserService = new UserService(userRepositoryMySQL)
  private val userRepositoryMongo: UserRepositoryMongoImpl = new UserRepositoryMongoImpl(MongoDBConnector.users)
  private val columnName: String = "spark_temp"
  private val columnType: String = "VARCHAR(255)"

  private def sparkWriteMysql(dfWrite: DataFrame
                              , tableName: String
                              , mode: SaveMode = SaveMode.Append): Unit = {

    if (dfWrite.isEmpty) {
      logger.warn(s"DataFrame is empty, nothing to write to $tableName")
      return
    }

    userService.alterTableAddColumn(tableName, columnName, columnType)

    try {
      dfWrite.write
        .format("jdbc")
        .option("url", config.mysql.jdbcUrl)
        .option("driver", "com.mysql.cj.jdbc.Driver")
        .option("dbtable", tableName)
        .option("user", config.mysql.user)
        .option("password", config.mysql.password)
        .mode(mode)
        .save()

      logger.info(s"Successfully wrote to MySQL table $tableName with mode $mode")
    } catch {
      case e: Exception =>
        logger.error(s"Failed to write to MySQL table $tableName", e)
        throw e
    }
  }

  private def sparkWriteMongDB(dfWrite: DataFrame
                               , collection: String
                               , mode: SaveMode = SaveMode.Append): Unit = {

    if (dfWrite.isEmpty) {
      logger.warn(s"DataFrame is empty, nothing to write to $collection")
      return
    }

    try {
      dfWrite.write
        .format("mongo")
        .option("uri", config.mongo.uri)
        .option("database", config.mongo.database)
        .option("collection", collection)
        .mode(mode)
        .save()

      logger.info(s"Successfully wrote to MongoDB table $collection with mode $mode")
    } catch {
      case e: Exception =>
        logger.error(s"Failed to write to MongoDB table $collection", e)
        throw e
    }
  }

  private def validateSparkMySQL(dfWrite: DataFrame
                                 , tableName: String
                                 , mode: SaveMode = SaveMode.Append): Unit = {
    if (dfWrite.isEmpty) {
      logger.warn(s"DataFrame is empty, nothing to write to $tableName")
      return
    }

    val subQuery: String = s"(SELECT * FROM $tableName WHERE spark_temp = 'spark_write') as sub_query"

    val dfRead: DataFrame = sparkSession.read
      .format("jdbc")
      .option("url", config.mysql.jdbcUrl)
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("dbtable", subQuery)
      .option("user", config.mysql.user)
      .option("password", config.mysql.password)
      .load()

    logger.info(s"Successfully read from MySQL with query: $subQuery")

    subTractDataFrame(dfWrite, dfRead, tableName, "MySQL", mode)

    dropColumnViaPlainJdbc(tableName, columnName)
  }

  private def validateSparkMongoDB(dfWrite: DataFrame
                                   , collection: String
                                   , mode: SaveMode = SaveMode.Append): Unit = {
    if (dfWrite.isEmpty) {
      logger.warn(s"DataFrame is empty, nothing to write to $collection")
      return
    }

    val query: String = """{"spark_temp": "spark_write"}"""
    val pipeline: String = s"""[{"$$match": $query}]"""

    val dfReadOne: DataFrame = sparkSession.read
      .format("mongo")
      .option("uri", config.mongo.uri)
      .option("database", config.mongo.database)
      .option("collection", collection)
      .option("pipeline", pipeline)
      .load()

    val dfRead: DataFrame = dfReadOne.select(
      col("user_id")
      , col("login")
      , col("gravatar_id")
      , col("avatar_url")
      , col("url")
      , col("spark_temp")
    )

    logger.info(s"Successfully read from Mongo with query: $query")

    subTractDataFrame(dfWrite, dfRead, collection, "MongoDB", mode)

    val result = Await.result(userRepositoryMongo.cleanUpSparkTempField(), 10.seconds)
    logger.info(s"Update result: ${result.getModifiedCount} records.")
  }

  private def subTractDataFrame(dfWrite: DataFrame
                                , dfRead: DataFrame
                                , destination: String
                                , dbType: String
                                , mode: SaveMode = SaveMode.Append
                                ): Unit = {
    dfWrite.cache
    dfRead.cache

    val writeCount: Long = dfWrite.count()
    val readCount: Long = dfRead.count()

    val diff: DataFrame = dfWrite.exceptAll(dfRead)

    if (writeCount == readCount && diff.isEmpty) {
      logger.info(s"Validate Success: 100% data is inserted")
    } else {
      logger.warn(s"Missing data: $writeCount -> $readCount")
      val missingCnt = diff.count()
      if (missingCnt > 0 ) {
        logger.warn(s"Insert $missingCnt missing data.....")
        try {
          if (dbType.toLowerCase == "mysql") {
            diff.write
              .format("jdbc")
              .option("url", config.mysql.jdbcUrl)
              .option("driver", "com.mysql.cj.jdbc.Driver")
              .option("dbtable", destination)
              .option("user", config.mysql.user)
              .option("password", config.mysql.password)
              .mode(mode)
              .save()
          } else if (dbType.toLowerCase == "mongodb"){
            diff.write
              .format("mongo")
              .option("uri", config.mongo.uri)
              .option("database", config.mongo.database)
              .option("collection", destination)
              .mode(mode)
              .save()
          } else {
            logger.warn("No Database defined")
          }
        } catch {
          case e: Exception =>
            logger.error(s"Failed to write to destination $destination", e)
            throw e
        }
      }
    }
  }

  private def dropColumnViaPlainJdbc(tableName: String, columnName: String): Unit = {
    import java.sql.{Connection, DriverManager}
    var conn: Connection = null
    try {
      conn = DriverManager.getConnection(
        config.mysql.jdbcUrl,
        config.mysql.user,
        config.mysql.password
      )
      val stmt = conn.createStatement()
      val sql = s"ALTER TABLE `$tableName` DROP COLUMN `$columnName`"
      val affected = stmt.executeUpdate(sql)
      logger.info(s"Has deleted `$columnName` from `$tableName` (affected: $affected)")
    } catch {
      case e: Exception =>
        logger.error(s"Can't deleted `$columnName` in table `$tableName`", e)
    } finally {
      if (conn != null && !conn.isClosed) conn.close()
    }
  }

  def writeAllDatabase(dfWrite: DataFrame
                      , destination: String
                      , mode: SaveMode = SaveMode.Append): Unit = {
    sparkWriteMysql(
      dfWrite = dfWrite
      , tableName = destination
      , mode = mode
    )

    sparkWriteMongDB(
      dfWrite = dfWrite
      , collection = destination
      , mode = mode
    )

    logger.info(s"Add success all data to $destination")
  }

  def validateAllDatabase(dfWrite: DataFrame
                          , destination: String
                          , mode: SaveMode = SaveMode.Append): Unit = {
    validateSparkMySQL(
      dfWrite = dfWrite
      , tableName = destination
      , mode = mode
    )

    validateSparkMongoDB(
      dfWrite = dfWrite
      , collection = destination
      , mode = mode
    )

    logger.info(s"Validate success all data from $destination")
  }
}
