ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

val sparkVersion = "3.5.4"
val slf4jVersion = "2.0.16"
val logBackVersion = "1.0.12"
val mongoVersion = "5.5.1"
val redisVersion = "4.3.2"

lazy val root = (project in file("."))
  .settings(
    name := "DATA-Synchronizing-Scala",

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % logBackVersion,
      "org.mongodb" % "mongodb-driver-sync" % mongoVersion,
      "redis.clients" % "jedis" % redisVersion,
      "com.typesafe" % "config" % "1.4.3",
      "com.typesafe.slick" %% "slick" % "3.4.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
      "mysql" % "mysql-connector-java" % "8.0.33",
      "org.mongodb.scala" %% "mongo-scala-driver" % "5.2.0",
      "org.mongodb.spark" %% "mongo-spark-connector" % "3.0.1"
    )
  )
