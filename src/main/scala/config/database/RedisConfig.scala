package config.database

case class RedisConfig(
                      host: String,
                      user: String,
                      password: String,
                      database: String,
                      port: Int
                      ) extends ValidateConfig {
  override def validate(): Unit = {
    val requiredFields: List[String] = List(
      if (host == null || host.isEmpty) Some("host") else None,
      if (user == null || user.isEmpty) Some("user") else None,
      if (password == null || password.isEmpty) Some("password") else None,
      if (database == null || database.isEmpty) Some("database") else None,
      if (port == null) Some("port") else None
    ).flatten

    if (requiredFields.nonEmpty) {
      val missingKeys = requiredFields.mkString(", ")
      throw new IllegalArgumentException(s"----------Missing config for RedisConfig: $missingKeys-------------")
    }
  }
}
