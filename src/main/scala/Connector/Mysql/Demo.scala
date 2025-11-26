package Connector.Mysql

import Connector.Mysql.Repository.UserRepositoryMySQLImpl
import Connector.Mysql.Service.UserService

object Demo extends App {

  val userRepository: UserRepositoryMySQLImpl = new UserRepositoryMySQLImpl
  private val userService: UserService = new UserService(userRepository)

  val requiredTables = Seq("Users")
  userService.createAndValidateMySqlSchema()
//  userService.validateRequiredTablesAction(Seq("Users"))

}
