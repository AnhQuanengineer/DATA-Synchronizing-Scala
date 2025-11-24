package Connector.Mysql.Service

import Connector.Mysql.DTO.User
import slick.jdbc.MySQLProfile.api._
import Connector.Mysql.MysqlConnector.{exec, close}
import Connector.Mysql.Repository.UserRepository
import slick.dbio.DBIO

import scala.concurrent.{ExecutionContext, Future}

class UserService(userRepository: UserRepository) {

  def createAndValidateMySqlSchema(): Unit = {
    exec(userRepository.createTable)
    exec(userRepository.insert(User(1L, "Nam", Some("nam@gmail.com"), Some("haha"), Some("huhu"))))
    val userWithId1: Option[User] = exec(userRepository.findById(1L))
    userWithId1 match {
      case Some(user) =>
        println(s"\nKết quả tìm kiếm: ${user}")
        if (user.login != "Nam") {
          throw new RuntimeException("Lỗi xác thực: Tên User không khớp.")
        }
        println("Kịch bản thành công: Insert và Find hoạt động đúng.")

      case None =>
        // Ném RuntimeException nếu không tìm thấy (thay cho ValueError trong Python)
        throw new RuntimeException("---------------------User not found sau khi insert-----------------------")
    }
    close()
  }

  def validateRequiredTablesAction(requiredTables: Seq[String])(implicit ec: ExecutionContext): DBIO[Unit] = {

    // 1. Thực thi SQL thô: SHOW TABLES
    // .as[String] yêu cầu Slick ánh xạ kết quả (tên bảng) thành Seq[String]
    val getTablesAction: DBIO[Seq[String]] = {
      sql"""SHOW TABLES""".as[(String)]
    }

    // 2. Xử lý kết quả (sử dụng flatMap để nối Action)
    getTablesAction.flatMap { existingTables =>
      // Tìm ra những bảng bị thiếu
      val missingTables = requiredTables.filterNot(existingTables.contains)

      if (missingTables.nonEmpty) {
        // Ném lỗi DBIO nếu thiếu bảng
        DBIO.failed(new RuntimeException(
          s"---------------------Missing tables in MySQL: ${missingTables.mkString(", ")}-----------------------"
        ))
      } else {
        // Nếu tất cả đều tồn tại, trả về thành công
        DBIO.successful(())
      }
    }
  }
}
