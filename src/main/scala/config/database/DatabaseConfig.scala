package config.database

case class DatabaseConfig(
                           mongo: MongoConfig,
                           mysql: MysqlConfig,
                           redis: RedisConfig
                         )
