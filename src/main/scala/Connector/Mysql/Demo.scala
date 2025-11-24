package Connector.Mysql

import Connector.Mysql.Repository.UserRepositoryImpl
import Connector.Mysql.Service.UserService

import scala.concurrent.ExecutionContext.Implicits.global

object Demo extends App {

  val userRepository: UserRepositoryImpl = new UserRepositoryImpl
  private val userService: UserService = new UserService(userRepository)

  val requiredTables = Seq("Users")
  userService.createAndValidateMySqlSchema()
//  userService.validateRequiredTablesAction(Seq("Users"))

}
