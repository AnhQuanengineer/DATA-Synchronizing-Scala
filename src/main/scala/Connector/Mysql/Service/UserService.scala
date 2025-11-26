package Connector.Mysql.Service

import Connector.Mysql.DTO.User
import slick.jdbc.MySQLProfile.api._
import Connector.Mysql.MysqlConnector.{exec, close}
import Connector.Mysql.Repository.UserRepositoryMySQL
import slick.dbio.DBIO

import scala.concurrent.{ExecutionContext, Future}

class UserService(userRepository: UserRepositoryMySQL) {

  def createAndValidateMySqlSchema(): Unit = {
    exec(userRepository.createTable)
    exec(userRepository.insert(User(1L, "Nam", Some("nam@gmail.com"), Some("haha"), Some("huhu"))))
    val userWithId1: Option[User] = exec(userRepository.findById(1L))
    userWithId1 match {
      case Some(user) =>
        println(s"\nResult: ${user}")
        if (user.login != "Nam") {
          throw new RuntimeException("Authentication error: User not right.")
        }
        println("Success: Insert and Find correct.")

      case None =>
        throw new RuntimeException("---------------------User not found after insert-----------------------")
    }
    close()
  }

  def validateRequiredTablesAction(requiredTables: Seq[String])(implicit ec: ExecutionContext): DBIO[Unit] = {

    // 1. Thực thi SQL thô: SHOW TABLES
    // .as[String] yêu cầu Slick ánh xạ kết quả (tên bảng) thành Seq[String]
    val getTablesAction: DBIO[Seq[String]] = {
      sql"""SHOW TABLES""".as[(String)]
    }

    getTablesAction.flatMap { existingTables =>
      // Tìm ra những bảng bị thiếu
      val missingTables = requiredTables.filterNot(existingTables.contains)

      if (missingTables.nonEmpty) {
        DBIO.failed(new RuntimeException(
          s"---------------------Missing tables in MySQL: ${missingTables.mkString(", ")}-----------------------"
        ))
      } else {
        DBIO.successful(())
      }
    }
  }

  def alterTableAddColumn(tableName: String, columnName: String, columnType: String): Unit = {
    try {
      val rowsAffected: Int = exec(userRepository.addColumn(tableName, columnName, columnType))

      println(s"----------Add column $columnName to $tableName in MySQL successfully. Rows affected: $rowsAffected-----------------")

    } catch {
      case e: Throwable =>
        throw new RuntimeException(s"--------Fail to ALTER TABLE $tableName in Mysql: ${e.getMessage}-----------", e)
    } finally {
      close()
    }
  }

  def alterTableDropColumn(tableName: String, columnName: String): Unit = {
    try {
      val rowsAffected: Int = exec(userRepository.dropColumn(tableName, columnName))

      println(s"----------Drop column $columnName to $tableName in MySQL successfully. Rows affected: $rowsAffected-----------------")

    } catch {
      case e: Throwable =>
        throw new RuntimeException(s"--------Fail to ALTER TABLE $tableName in Mysql: ${e.getMessage}-----------", e)
    } finally {
      close()
    }
  }
}
