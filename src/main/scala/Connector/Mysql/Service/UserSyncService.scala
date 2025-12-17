package Connector.Mysql.Service

import Connector.Mysql.DTO.UserLogEntry
import Connector.Mysql.MysqlConnector.run
import Connector.Mysql.Repository.UserRepositoryMySQL
import Connector.Mysql.TimestampHandler
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}

class UserSyncService(userRepository: UserRepositoryMySQL)(implicit ec: ExecutionContext) {

  def getDataTriggerService(): Future[(Seq[UserLogEntry], String)] = {

    val lastTimestamp = TimestampHandler.loadLastTimestamp()
    println(s"[SYNC] Bắt đầu đồng bộ với lastTimestamp = '$lastTimestamp'")

    val dbio: DBIO[(Seq[UserLogEntry], String)] = userRepository.getDataTrigger(lastTimestamp)

    run(dbio.transactionally)
      .map { case (newLogs, newTs) =>
        if (newLogs.nonEmpty) {
          println(s"SYNC THÀNH CÔNG: ${newLogs.size} bản ghi mới")
          newLogs.take(5).foreach(entry => println(s"  → $entry"))
        } else {
          println("Không có dữ liệu mới lần này.")
        }

        // Quan trọng: vẫn luôn lưu timestamp mới nhất
        TimestampHandler.saveLastTimestamp(newTs)
        println(s"Đã cập nhật timestamp → $newTs")

        // Trả về kết quả cho caller
        (newLogs, newTs)
      }
      .recover { case ex: Throwable =>
        println(s"[LỖI SYNC] ${ex.getClass.getSimpleName}: ${ex.getMessage}")
        ex.printStackTrace()

        // Vẫn cố lưu timestamp cũ để lần sau còn chạy tiếp
        TimestampHandler.saveLastTimestamp(lastTimestamp)
        println(s"Đã giữ lại timestamp cũ: $lastTimestamp")

        // Trả về danh sách rỗng + timestamp cũ khi lỗi
        (Seq.empty[UserLogEntry], lastTimestamp)
      }
  }
}
