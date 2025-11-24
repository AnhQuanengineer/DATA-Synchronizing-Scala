package Connector

trait DatabaseConnector {
  def connect[T]() : T
  def disconnect(): Unit
  def reconnect(): Unit
}
