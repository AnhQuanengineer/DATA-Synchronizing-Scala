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
}