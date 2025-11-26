package Spark

import Connector.Spark.SparkConnector
import Ultis.SparkWriteData
import config.database.{ConfigLoader, DatabaseConfig}
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.types._

object MainSpark extends App {

  private val INPUT_JSON_PATH: String = "2015-03-01-17.json"

  val jars: List[String] = List(
    "/home/victo/PycharmProjects/Big_data_project/lib/mongo-spark-connector_2.12-3.0.1.jar",
    "/home/victo/PycharmProjects/Big_data_project/lib/mongodb-driver-sync-4.0.5.jar",
    "/home/victo/PycharmProjects/Big_data_project/lib/bson-4.0.5.jar",
    "/home/victo/PycharmProjects/Big_data_project/lib/mongodb-driver-core-4.0.5.jar"
  )

  val jarPackages: List[String] = List (
    "org.mongodb.spark:mongo-spark-connector_2.12:3.0.1"
  )

  private val sparkConf: Map[String, String] = Map (
    "spark.ui.showConsoleProgress" -> "false"
  )

  private val sparkConnect: SparkConnector = new SparkConnector(
    appName = "quanDz"
    , masterUrl= "local[*]"
    , executorMemory = "4g"
    , executorCores = 2
    , driverMemory = "2g"
    , numExecutors = 3
    , jars = None
    , jarPackages = Some(jarPackages)
    , extraSparkConf = Some(sparkConf)
    , logLevel = "INFO"
  )

  val sparkSession: SparkSession = sparkConnect.spark

  private val USER_SCHEMA: StructType = StructType(
    Array(
      StructField("id", LongType, nullable = false),
      StructField("login", StringType, nullable = true),
      StructField("gravatar_id", StringType, nullable = true),
      StructField("url", StringType, nullable = true),
      StructField("avatar_url", StringType, nullable = true),
    )
  )

  private val REPO_SCHEMA: StructType = StructType(
    Array(
      StructField("id", LongType, nullable = false),
      StructField("name", StringType, nullable = true),
      StructField("url", StringType, nullable = true),
    )
  )

  private val GITHUB_SCHEMA: StructType = StructType(
    Array(
      StructField("actor", USER_SCHEMA, nullable = true),
      StructField("repo", REPO_SCHEMA, nullable = true)
    )
  )

  private val df: DataFrame = sparkSession.read
    .schema(GITHUB_SCHEMA)
    .json(INPUT_JSON_PATH)

  df.show()

  private val userDf: DataFrame = df.withColumn("spark_temp", lit("spark_write"))
    .select(
    col("actor.id").alias("user_id")
    ,col("actor.login").alias("login")
    ,col("actor.gravatar_id").alias("gravatar_id")
    ,col("actor.avatar_url").alias("avatar_url")
    ,col("actor.url").alias("url")
    ,col("spark_temp")
  )

  userDf.show(truncate = false)

  private val repoDf = df.select(
    col("repo.id").alias("repo_id")
    ,col("repo.name").alias("name")
    ,col("repo.url").alias("url")
  )

  repoDf.show(truncate = false)

  val dbConf: DatabaseConfig = ConfigLoader.getDatabaseConfig

  private val writeMySQL: SparkWriteData = new SparkWriteData(sparkSession = sparkSession, config = dbConf)
  writeMySQL.writeAllDatabase(userDf, "users", SaveMode.Append)
  writeMySQL.validateAllDatabase(userDf, "users", SaveMode.Append)

  sparkSession.stop()
}
