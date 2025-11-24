package Connector.MongoDB.Service

import Connector.MongoDB.Repository.UserRepository
import Connector.MongoDB.DTO.User
import org.mongodb.scala.result.InsertOneResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class UserService(userRepository: UserRepository) {

  def createAndValidateMongoSchema(): Unit = {
    val teo = User(
      user_id = 1L,
      login = "Nam",
      gravatar_id = Some("nam@gmail.com"),
      avatar_url = Some("haha"),
      url = Some("huhu")
    )

    // Insert trước, chờ kết quả
    val insertFuture: Future[InsertOneResult] = userRepository.insertOne(teo)

    // Chờ insert xong → find
    val validateFuture: Future[Option[User]] = insertFuture.flatMap { insertResult =>
      println(s"Insert OK, Mongo _id: ${insertResult.getInsertedId.asObjectId().getValue.toHexString}")
      userRepository.findById(1L)
    }

    validateFuture.onComplete {
      case Success(maybeUser) =>
        maybeUser match {
          case Some(user) =>
            println(s"Validate thành công! User: ${user.login}, id = ${user.user_id}")
          case None =>
            println("Validate ko thành công – không tìm thấy user vừa insert")
        }

      case Failure(e) =>
        println(s"Lỗi truy vấn: ${e.getMessage}")
        e.printStackTrace()
    }

    // Chờ trong demo (xóa trong production)
    Await.result(validateFuture, 10.seconds)
  }
}
