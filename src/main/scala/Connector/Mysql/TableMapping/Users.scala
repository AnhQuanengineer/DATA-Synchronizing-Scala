package Connector.Mysql.TableMapping
import Connector.Mysql.DTO.User
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape


class Users (tag: Tag) extends Table[User](tag, "users"){
  def user_id: Rep[Long] = column[Long]("id")
//  Nó cho Slick biết rằng có một cột trong bảng CSDL có tên là "name" và chứa dữ liệu kiểu String
  def login: Rep[String] = column[String]("login")
  def gravatar_id: Rep[String] = column[String]("gravatar_id")
  def avatar_url: Rep[String] = column[String]("avatar_url")
  def url: Rep[String] = column[String]("url")

  override def * : ProvenShape[User] = (user_id, login, gravatar_id.?, avatar_url.?, url.?) <> (User.tupled, User.unapply)
}
