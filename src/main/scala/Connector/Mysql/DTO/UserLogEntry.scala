package Connector.Mysql.DTO

case class UserLogEntry(
                       user_id: Long
                       , login: String
                       , gravatar_id: Option[String] = None
                       , avatar_url: Option[String] = None
                       , url: Option[String] = None
                       , state: String
                       , log_timestamp: String
                       )
