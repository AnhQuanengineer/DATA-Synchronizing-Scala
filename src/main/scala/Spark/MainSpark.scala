package Spark

import Connector.Spark.SparkConnector
import org.apache.spark.sql.SparkSession

object MainSpark extends App {

  val jars: List[String] = List(
    "/home/victo/PycharmProjects/Big_data_project/lib/mongo-spark-connector_2.12-3.0.1.jar",
    "/home/victo/PycharmProjects/Big_data_project/lib/mongodb-driver-sync-4.0.5.jar",
    "/home/victo/PycharmProjects/Big_data_project/lib/bson-4.0.5.jar",
    "/home/victo/PycharmProjects/Big_data_project/lib/mongodb-driver-core-4.0.5.jar"
  )

  val jarPackages: List[String] = List (
    "org.mongodb.spark:mongo-spark-connector_2.12:3.0.1"
  )

  val sparkConf: Map[String, String] = Map (
    "spark.ui.showConsoleProgress" -> "false"
  )


  val sparkConnect: SparkConnector = new SparkConnector(
    appName = "quandz"
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

  sparkSession.stop()
}
