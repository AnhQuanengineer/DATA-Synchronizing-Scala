package config.database
import com.typesafe.config.{Config, ConfigFactory}

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
      , url = config.getString("Mysql.url")
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
    println(dbConf.mysql.driver)
//    val config = ConfigFactory.load()
//    println(config.getString("Mysql.host"))
  }
}
