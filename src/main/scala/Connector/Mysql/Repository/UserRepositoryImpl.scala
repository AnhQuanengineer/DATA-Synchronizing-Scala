package Connector.Mysql.Repository
import Connector.Mysql.DTO.User
import Connector.Mysql.TableMapping.Users
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._

class UserRepositoryImpl extends UserRepository {

  private val users = TableQuery[Users]

  override def createTable: DBIO[Unit]       = users.schema.createIfNotExists

  override def insert(user: User): DBIO[Int]       = users += user

  override def insertBatch(newUsers: Seq[User]): DBIO[Option[Int]] = users ++= newUsers

  override def findAll(): DBIO[Seq[User]]      = users.result

  override def findById(user_id: Long): DBIO[Option[User]] = users.filter(_.user_id === user_id).result.headOption

  override def update(user: User): DBIO[Int]   = users.filter(_.user_id === user.user_id).update(user)

  override def delete(user_id: Long): DBIO[Int]     = users.filter(_.user_id === user_id).delete

}