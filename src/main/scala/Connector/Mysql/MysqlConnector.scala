package Connector.Mysql

import config.database.{ConfigLoader, DatabaseConfig}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

object MysqlConnector {
  val dbConf: DatabaseConfig = ConfigLoader.getDatabaseConfig
  private val db = Database.forURL(
    url = s"jdbc:mysql://${dbConf.mysql.host}:${dbConf.mysql.port}/${dbConf.mysql.database}?createDatabaseIfNotExist=true",
    user = dbConf.mysql.user,
    password = dbConf.mysql.password,
  )

  /*
  DBIO[T]: một đối tượng "mô tả" thao tác bạn muốn thực hiện (ví dụ: insert, select, update, delete
   */
//  Hàm run được dùng để thực thi thao tác CSDL một cách bất đồng bộ
  def run[T](action: DBIO[T]): Future[T] = db.run(action)
//  Hàm exec được dùng để thực thi thao tác CSDL một cách đồng bộ
  def exec[T](action: DBIO[T]): T =
    scala.concurrent.Await.result(db.run(action), scala.concurrent.duration.Duration.Inf)

  def close(): Unit = {
    db.close()
    println("Database connect is off")
  }
}
