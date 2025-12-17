package Connector.Mysql

import Connector.Mysql.MysqlConnector.close
import Connector.Mysql.Repository.UserRepositoryMySQLImpl
import Connector.Mysql.Service.UserSyncService

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object Test extends App {
  println("Last timestamp: " + TimestampHandler.loadLastTimestamp())
  println("=== BẮT ĐẦU SYNC 1 LẦN ===")

  val service = new UserSyncService(userRepository = new UserRepositoryMySQLImpl)

  val resultFuture = service.getDataTriggerService()

  val (logs, newTimestamp) = Await.result(resultFuture, 60.seconds)

  println(s"\n=== HOÀN TẤT ===")
  println(s"Đã nhận được ${logs.size} bản ghi")
  println(s"Timestamp mới nhất: $newTimestamp")

  // Nếu muốn in thử dữ liệu
  logs.take(10).foreach(println)

  close()
}
