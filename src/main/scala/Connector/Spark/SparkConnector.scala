package Connector.Spark

import org.apache.logging.log4j.{LogManager, Logger}
import org.apache.spark.sql.SparkSession

class SparkConnector (
                       appName: String,
                       masterUrl: String,
                       executorMemory: String = "4g",
                       executorCores: Int = 2,
                       driverMemory: String = "2g",
                       numExecutors: Int = 3,
                       jars: Option[Seq[String]] = None,
                       jarPackages: Option[Seq[String]] = None,
                       extraSparkConf: Option[Map[String, String]] = None,
                       logLevel: String = "INFO"
                     ) {
  private val logger: Logger = LogManager.getLogger(getClass)
  /*
  khi nào tạo object mà có truy cập biến thì nó mới tạo
   */
  private lazy val sparkSession: SparkSession = {
    logger.info(s"Starting SparkSession '$appName' on $masterUrl")

    val builder = SparkSession.builder()
      .appName(appName)
      .master(masterUrl)
      .config("spark.executor.memory", executorMemory)
      .config("spark.executor.cores", executorCores)
      .config("spark.driver.memory", driverMemory)
      .config("spark.executor.instances", numExecutors)

    if (jars.nonEmpty) {
      builder.config("spark.jars", jars.mkString(","))
    }

    if (jarPackages.nonEmpty) {
      builder.config("spark.jars.packages", jarPackages.mkString(","))
    }

    extraSparkConf.getOrElse(Map.empty).foreach { case (k, v) => builder.config(k, v) }

    val session = builder.getOrCreate()
    session.sparkContext.setLogLevel(logLevel)
    logger.info(s"SparkSession '$appName' started successfully")
    session
  }

  def spark: SparkSession = sparkSession

  def stop(): Unit ={
    if (sparkSession != null) {
      logger.info(s"Stopping SparkSession '$appName'")
      sparkSession.stop()
      SparkSession.clearActiveSession()
      SparkSession.clearDefaultSession()
    }
  }
}
