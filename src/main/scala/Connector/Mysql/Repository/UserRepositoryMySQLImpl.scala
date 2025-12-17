package Connector.Mysql.Repository
import Connector.Mysql.DTO.{User, UserLogEntry}
import Connector.Mysql.TableMapping.Users
import slick.jdbc.MySQLProfile
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

class UserRepositoryMySQLImpl extends UserRepositoryMySQL {

  private val users = TableQuery[Users]

  override def createTable: DBIO[Unit]       = users.schema.createIfNotExists

  override def insert(user: User): DBIO[Int]       = users += user

  override def insertBatch(newUsers: Seq[User]): DBIO[Option[Int]] = users ++= newUsers

  override def findAll(): DBIO[Seq[User]]      = users.result

  override def findById(user_id: Long): DBIO[Option[User]] = users.filter(_.user_id === user_id).result.headOption

  override def update(user: User): DBIO[Int]   = users.filter(_.user_id === user.user_id).update(user)

  override def delete(user_id: Long): DBIO[Int]     = users.filter(_.user_id === user_id).delete

  /**
   * Thực thi lệnh SQL thô để thêm cột vào bảng.
   * @param tableName Tên bảng cần ALTER.
   * @param columnName Tên cột mới.
   * @param columnType Kiểu dữ liệu của cột (ví dụ: VARCHAR(255), INT, DATE).
   * @return DBIO[Int] - Kết quả là số lượng hàng bị ảnh hưởng (thường là 0 cho ALTER TABLE).
   *         asUpdate chuyển câu sql thành lệnh DDL/DML
   */
  override def addColumn(tableName: String, columnName: String, columnType: String): DBIO[Int] = {
    // Sử dụng s-interpolator của Slick để xây dựng lệnh SQL an toàn.
    val alterSql = sql"ALTER TABLE #$tableName ADD COLUMN #$columnName #$columnType".asUpdate
    // .asUpdate được sử dụng cho các lệnh thay đổi dữ liệu/schema (INSERT, UPDATE, DELETE, ALTER).
    alterSql
  }

  override def dropColumn(tableName: String, columnName: String): DBIO[Int] = {
    // Sử dụng s-interpolator của Slick để xây dựng lệnh SQL an toàn.
    val alterSql = sql"ALTER TABLE #$tableName DROP COLUMN #$columnName".asUpdate
    // .asUpdate được sử dụng cho các lệnh thay đổi dữ liệu/schema (INSERT, UPDATE, DELETE, ALTER).
    alterSql
  }

  override def getDataTrigger(lastTimestamp: String): DBIO[(Seq[UserLogEntry], String)] = {
    val hasTimestamp = lastTimestamp.trim.nonEmpty
    val query = if (hasTimestamp) {
      sql"""
       SELECT
         user_id,
         login,
         gravatar_id,
         avatar_url,
         url,
         state,
         DATE_FORMAT(log_timestamp, '%Y-%m-%d %H:%i:%s.%f') AS formatted_ts
       FROM user_log_after
       WHERE log_timestamp > STR_TO_DATE($lastTimestamp, '%Y-%m-%d %H:%i:%s.%f')
       ORDER BY log_timestamp ASC
       """
    } else {
      sql"""
       SELECT
         user_id,
         login,
         gravatar_id,
         avatar_url,
         url,
         state,
         DATE_FORMAT(log_timestamp, '%Y-%m-%d %H:%i:%s.%f') AS formatted_ts
       FROM user_log_after
       ORDER BY log_timestamp ASC
       """
    }

    // Thực thi và map kết quả
    query
      .as[(Long, String, Option[String], Option[String], Option[String], String, String)]
      .map { rows =>
        if (rows.isEmpty) {
          (Seq.empty[UserLogEntry], lastTimestamp)
        } else {
          val entries = rows.map { case (user_id, login, gravatar_id, avatar_url, url, state, ts) =>
            UserLogEntry(
              user_id       = user_id,
              login         = login,
              gravatar_id   = gravatar_id,
              avatar_url    = avatar_url,
              url           = url,
              state         = state,
              log_timestamp = ts
            )
          }

          val newMaxTimestamp = rows.last._7

          (entries, newMaxTimestamp)
        }
      }
      .withPinnedSession // quan trọng khi dùng trong transaction
  }
}