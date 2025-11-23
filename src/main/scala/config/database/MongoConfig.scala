package config.database

case class MongoConfig(
                      uri: String,
                      database: String,
                      collection: String
                      ) extends ValidateConfig {
  override def validate(): Unit = {
    val requiredFields = List(
      if (uri == null || uri.isEmpty) Some("uri") else None,
      if (database == null || database.isEmpty) Some("dbName") else None,
    ).flatten

    if (requiredFields.nonEmpty) {
      val missingKeys = requiredFields.mkString(", ")
      throw new IllegalArgumentException(s"----------Missing config for MongoDBConfig: $missingKeys-------------")
    }
  }
}
