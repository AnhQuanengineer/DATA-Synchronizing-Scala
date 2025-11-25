package Ultis

import Connector.Mysql.Repository.UserRepositoryImpl
import Connector.Mysql.Service.UserService
import config.database.DatabaseConfig
import org.apache.spark.sql.{DataFrame, SparkSession, SaveMode}
import org.slf4j.LoggerFactory

class SparkWriteData(sparkSession: SparkSession, config: DatabaseConfig) {

  private val logger = LoggerFactory.getLogger(getClass)
  private val userRepository: UserRepositoryImpl = new UserRepositoryImpl
  private val userService: UserService = new UserService(userRepository)
  private val columnName: String = "spark_temp"
  private val columnType: String = "VARCHAR(255)"

  def sparkWriteMysql(dfWrite: DataFrame
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

  def validateSparkMySQL(dfWrite: DataFrame
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

    subTractDataFrame(dfWrite, dfRead, tableName, mode)
  }

  private def subTractDataFrame(dfWrite: DataFrame
                                , dfRead: DataFrame
                                , tableName: String
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
          diff.write
            .format("jdbc")
            .option("url", config.mysql.jdbcUrl)
            .option("driver", "com.mysql.cj.jdbc.Driver")
            .option("dbtable", tableName)
            .option("user", config.mysql.user)
            .option("password", config.mysql.password)
            .mode(mode)
            .save()
        } catch {
          case e: Exception =>
            logger.error(s"Failed to write to MySQL table $tableName", e)
            throw e
        }
      }
    }
//    userService.alterTableDropColumn(tableName, columnName)
    dropColumnViaPlainJdbc(tableName, columnName)
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
        logger.error(s"Không thể xóa cột `$columnName` trong bảng `$tableName`", e)
      // Có thể chọn throw hoặc không throw tùy bạn
      // throw e
    } finally {
      if (conn != null && !conn.isClosed) conn.close()
    }
  }
}
