package Connector.MongoDB.Repository

import Connector.MongoDB.DTO.User
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates
import org.mongodb.scala.model.Updates.combine
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import org.mongodb.scala.result.InsertOneResult

class UserRepositoryImpl(users: MongoCollection[User]) extends UserRepository {

  override def insertOne(record: User): Future[InsertOneResult] = {
    users.insertOne(record)
      .head()  // Observable[InsertOneResult] → Future[InsertOneResult]
      .andThen {  // Log kết quả (không block)
        case Success(result) =>
          val insertedId = result.getInsertedId.asObjectId().getValue.toHexString  // Lấy _id tự sinh
          println(s">>> Chèn thành công user_id = ${record.user_id} (login: ${record.login}), Mongo _id: $insertedId <<<")

        case Failure(e) =>
          e match {
            case _: com.mongodb.MongoSecurityException =>
              println(s"LỖI XÁC THỰC MONGO! ${e.getMessage} (kiểm tra user/pass + ?authSource=admin)")
            case _ =>
              println(s"LỖI INSERT user_id = ${record.user_id}: ${e.getMessage}")
              e.printStackTrace()
          }
      }
  }

  override def insertMany(records: Seq[User]): Unit = {
    users.insertMany(records)
    println("Đã thêm nhiều bản ghi.")
    readAll()
  }

  override def readAll(): Unit = {
    println("\nREAD")
    users.find().toFuture().onComplete {
      case Success(all) =>
        println(s"Tổng cộng: ${all.length} người")
        all.foreach(u => println(s"Read all: ${u.login} - ${u.gravatar_id} - ${u.avatar_url} - ${u.url}"))
      case Failure(e) => println(e)
    }
  }

  override def findById(user_id: Long): Future[Option[User]] = {
    users
      .find(equal("user_id", user_id))
      .first()
      .headOption()   // → Future[Option[User]] luôn, không cần map!
  }

  override def updateById(user_id: Long): Unit = {
    println("\nUPDATE")
    users.updateOne(
      equal("user_id", user_id),
      combine(Updates.set("login", "QUAN"))
    ).head().foreach { res =>
      println(s"Cập nhật → modified: ${res.getModifiedCount}")
    }
  }

//  override def deleteOne(): Unit = {
//    println("\nDELETE")
//    users.deleteOne(equal("name", "Trần Văn Nam")).head()
//      .foreach(r => println(s"Xóa Nam → deleted: ${r.getDeletedCount}"))
//
//    users.deleteMany(equal("active", false)).head()
//      .foreach(r => println(s"Xóa hết người inactive → deleted: ${r.getDeletedCount}"))
//      .foreach(_ => finalResult())
//  }
}
