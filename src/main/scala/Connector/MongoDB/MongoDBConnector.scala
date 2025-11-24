package Connector.MongoDB

import config.database.{ConfigLoader, DatabaseConfig}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import Connector.MongoDB.DTO.User

object MongoDBConnector {
  val dbConf: DatabaseConfig = ConfigLoader.getDatabaseConfig
  private val uri = dbConf.mongo.uri

  private val client: MongoClient = MongoClient(uri)
  private val codecRegistry = fromRegistries(fromProviders(User.codecProvider), DEFAULT_CODEC_REGISTRY)
  private val db = client.getDatabase(dbConf.mongo.database).withCodecRegistry(codecRegistry)
  val users: MongoCollection[User] = db.getCollection[User](dbConf.mongo.collection)
}
