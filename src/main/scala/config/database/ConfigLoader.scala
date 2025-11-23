package config.database
import com.typesafe.config.{Config, ConfigFactory}
import config.database.ConfigLoader.config

object ConfigLoader {
  val config: Config = ConfigFactory.load()

  private def loadMongoConfig(): MongoConfig = {
    val mongoConfig = MongoConfig(
      uri = config.getString("Mongo.uri")
      , database = config.getString("Mongo.database")
      , collection = config.getString("Mongo.collection")
    )

    mongoConfig.validate()

    mongoConfig
  }

  private def loadMySQLConfig(): MysqlConfig = {
    val mysqlConfig = MysqlConfig(
      host = config.getString("Mysql.host")
      , port = config.getInt("Mysql.port")
      , user = config.getString("Mysql.user")
      , password = config.getString("Mysql.password")
      , database = config.getString("Mysql.database")
      , jdbc = config.getString("Mysql.jdbc")
      , driver = config.getString("Mysql.driver")
    )

    mysqlConfig.validate()

    mysqlConfig
  }

  private def loadRedisConfig(): RedisConfig = {
    val redisConfig = RedisConfig(
      host = config.getString("Redis.host")
      , user = config.getString("Redis.user")
      , password = config.getString("Redis.password")
      , database = config.getString("Redis.database")
      , port = config.getInt("Redis.port")
    )

    redisConfig.validate()

    redisConfig
  }

  def getDatabaseConfig :DatabaseConfig = {
    val mongoConf = loadMongoConfig()
    val mysqlConf = loadMySQLConfig()
    val redisConf = loadRedisConfig()

    DatabaseConfig(mongoConf
      , mysqlConf
      , redisConf)
  }

  def main(args: Array[String]): Unit = {
    val dbConf = getDatabaseConfig
    println(dbConf.mongo.uri)
//    val config = ConfigFactory.load()
//    println(config.getString("Mysql.host"))
  }
}
