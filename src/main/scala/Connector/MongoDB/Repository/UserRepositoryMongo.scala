package Connector.MongoDB.Repository

import Connector.MongoDB.DTO.User
import org.mongodb.scala.result.{InsertOneResult, UpdateResult}

import scala.concurrent.Future

trait UserRepositoryMongo {
  def insertOne(record: User): Future[InsertOneResult]

  def insertMany(records: Seq[User]): Unit

  def readAll(): Unit

  def findById(user_id: Long): Future[Option[User]]

  def updateById(user_id: Long): Unit

  def cleanUpSparkTempField(): Future[UpdateResult]

//  def deleteOne()
//
//  def deleteMany()

}
