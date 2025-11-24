package Connector.MongoDB.Repository

import Connector.MongoDB.DTO.User
import org.mongodb.scala.result.InsertOneResult

import scala.concurrent.Future

trait UserRepository {
  def insertOne(record: User): Future[InsertOneResult]

  def insertMany(records: Seq[User]): Unit

  def readAll(): Unit

  def findById(user_id: Long): Future[Option[User]]

  def updateById(user_id: Long): Unit

//  def deleteOne()
//
//  def deleteMany()

}
