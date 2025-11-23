package config.database

case class MysqlConfig(
                      host: String,
                      port: Int,
                      user: String,
                      password: String,
                      database: String,
                      jdbc: String,
                      driver: String
                      ) extends ValidateConfig {
  override def validate(): Unit = {
    val requiredFields: List[String] = List(
      if (host == null || host.isEmpty) Some("host") else None,
      if (user == null || user.isEmpty) Some("user") else None,
      if (password == null || password.isEmpty) Some("password") else None,
      if (database == null || database.isEmpty) Some("database") else None,
      if (port == null) Some("port") else None,
      if (jdbc == null || jdbc.isEmpty) Some("jdbc") else None,
      if (driver == null || driver.isEmpty) Some("driver") else None,
    ).flatten

    if (requiredFields.nonEmpty) {
      val missingKeys = requiredFields.mkString(", ")
      throw new IllegalArgumentException(s"----------Missing config for MySQLConfig: $missingKeys-------------")
    }
  }
}
