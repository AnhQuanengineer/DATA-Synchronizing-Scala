package Connector.MongoDB

import Connector.MongoDB.Repository.UserRepositoryImpl
import Connector.MongoDB.Service.UserService


object Demo extends App{
  val userRepository: UserRepositoryImpl = new UserRepositoryImpl(MongoDBConnector.users)
  private val userService: UserService = new UserService(userRepository)

  userService.createAndValidateMongoSchema()

}
