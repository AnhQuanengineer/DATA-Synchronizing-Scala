package Connector.Mysql

import config.database.{ConfigLoader, DatabaseConfig}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.util.Try
import scala.collection.JavaConverters._

object TimestampHandler {
  val dbConf: DatabaseConfig = ConfigLoader.getDatabaseConfig
  private val TIMESTAMP_FILE: String = dbConf.mysql.log_timestamp
  private val FALLBACK_TIMESTAMP = ""


  private def ensureDirectoryExists(filePath: String): Unit = {
    val path = Paths.get(filePath)
    val parent = path.getParent
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent)
    }
  }

  def loadLastTimestamp(): String = {
    val path = Paths.get(TIMESTAMP_FILE)

    if (Files.exists(path) && Files.isRegularFile(path)) {
      Try {
        val lines = Files.readAllLines(path, StandardCharsets.UTF_8).asScala
        if (lines.nonEmpty) lines.head.trim else FALLBACK_TIMESTAMP
      }.getOrElse(FALLBACK_TIMESTAMP)
    } else {
      FALLBACK_TIMESTAMP
    }
  }

  def saveLastTimestamp(ts: String): Unit = {
    ensureDirectoryExists(TIMESTAMP_FILE)
    val path = Paths.get(TIMESTAMP_FILE)

    val content = java.util.Collections.singleton(ts)
    Files.write(
      path
      , content
      , StandardCharsets.UTF_8
      , StandardOpenOption.CREATE
      , StandardOpenOption.WRITE
      , StandardOpenOption.TRUNCATE_EXISTING
    )
  }
}
