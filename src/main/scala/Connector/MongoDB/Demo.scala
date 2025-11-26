package Connector.MongoDB

import Connector.MongoDB.Repository.UserRepositoryMongoImpl
import Connector.MongoDB.Service.UserService


object Demo extends App{
  val userRepository: UserRepositoryMongoImpl = new UserRepositoryMongoImpl(MongoDBConnector.users)
  private val userService: UserService = new UserService(userRepository)

  userService.createAndValidateMongoSchema()

}
