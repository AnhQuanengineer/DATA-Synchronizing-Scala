package Connector.Mysql.Repository
import Connector.Mysql.DTO.User
import slick.jdbc.MySQLProfile.api._

trait UserRepository {
  def createTable: DBIO[Unit]
  def insert(user: User): DBIO[Int]
  def insertBatch(newUsers: Seq[User]): DBIO[Option[Int]]
  def findAll(): DBIO[Seq[User]]
  def findById(user_id: Long): DBIO[Option[User]]
  def update(user: User): DBIO[Int]
  def delete(user_id: Long): DBIO[Int]
  def addColumn(tableName: String, columnName: String, columnType: String): DBIO[Int]
  def dropColumn(tableName: String, columnName: String): DBIO[Int]
}
