package config.database

case class MysqlConfig(
                      host: String,
                      port: Int,
                      user: String,
                      password: String,
                      database: String,
                      url: String,
                      driver: String,
                      log_timestamp: String
                      ) extends ValidateConfig {

  def jdbcUrl: String = {
    s"jdbc:mysql://$host:$port/$database"
  }

  override def validate(): Unit = {
    val requiredFields: List[String] = List(
      if (host == null || host.isEmpty) Some("host") else None,
      if (user == null || user.isEmpty) Some("user") else None,
      if (password == null || password.isEmpty) Some("password") else None,
      if (database == null || database.isEmpty) Some("database") else None,
      if (port == null) Some("port") else None,
      if (url == null || url.isEmpty) Some("url") else None,
      if (driver == null || driver.isEmpty) Some("driver") else None,
      if (log_timestamp == null || log_timestamp.isEmpty) Some("log_timestamp") else None
    ).flatten

    if (requiredFields.nonEmpty) {
      val missingKeys = requiredFields.mkString(", ")
      throw new IllegalArgumentException(s"----------Missing config for MySQLConfig: $missingKeys-------------")
    }
  }
}
