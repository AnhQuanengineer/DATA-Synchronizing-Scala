package Connector.MongoDB.DTO

import org.bson.codecs.configuration.CodecProvider
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros

case class User(
               _id: ObjectId = new ObjectId()
               , user_id: Long
               , login: String
               , gravatar_id: Option[String] = None
               , avatar_url: Option[String]= None
               , url: Option[String] = None
               )

object User {
  val codecProvider: CodecProvider = Macros.createCodecProviderIgnoreNone[User]()
}
