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

  def alterTableAddColumn(tableName: String, columnName: String, columnType: String): Unit = {
    try {
      // 1. Thực thi DBIO Action bằng hàm exec
      val rowsAffected: Int = exec(userRepository.addColumn(tableName, columnName, columnType))

      // 2. In kết quả thành công (tương đương với print)
      println(s"----------Add column $columnName to $tableName in MySQL successfully. Rows affected: $rowsAffected-----------------")

    } catch {
      case e: Throwable =>
        // 3. Xử lý lỗi (tương đương với raise Exception)
        // Lưu ý: Hàm exec() của bạn có thể đã bao gồm logic connect/close/commit/rollback
        // Nếu không, cần đảm bảo DBIO Action được thực thi trong một transaction.
        throw new RuntimeException(s"--------Fail to ALTER TABLE $tableName in Mysql: ${e.getMessage}-----------", e)
    } finally {
      // Đóng kết nối nếu hàm exec không tự làm
      close()
    }
  }

  def alterTableDropColumn(tableName: String, columnName: String): Unit = {
    try {
      // 1. Thực thi DBIO Action bằng hàm exec
      val rowsAffected: Int = exec(userRepository.dropColumn(tableName, columnName))

      // 2. In kết quả thành công (tương đương với print)
      println(s"----------Drop column $columnName to $tableName in MySQL successfully. Rows affected: $rowsAffected-----------------")

    } catch {
      case e: Throwable =>
        // 3. Xử lý lỗi (tương đương với raise Exception)
        // Lưu ý: Hàm exec() của bạn có thể đã bao gồm logic connect/close/commit/rollback
        // Nếu không, cần đảm bảo DBIO Action được thực thi trong một transaction.
        throw new RuntimeException(s"--------Fail to ALTER TABLE $tableName in Mysql: ${e.getMessage}-----------", e)
    } finally {
      // Đóng kết nối nếu hàm exec không tự làm
      close()
    }
  }
}
