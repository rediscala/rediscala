package redis.commands

import redis.ByteStringDeserializer
import redis.ByteStringSerializer
import redis.Request
import scala.concurrent.Future
import redis.protocol.Status
import redis.api.connection._

trait Connection extends Request {
  def auth[V: ByteStringSerializer](password: V): Future[Status] =
    send(Auth(password))

  def auth[V: ByteStringSerializer](username: V, password: V): Future[Status] =
    send(Auth(username, Some(password)))

  def echo[V: ByteStringSerializer, R: ByteStringDeserializer](value: V): Future[Option[R]] =
    send(Echo(value))

  def ping(): Future[String] =
    send(Ping)

  // commands sent after will fail with [[redis.protocol.NoConnectionException]]
  def quit(): Future[Boolean] =
    send(Quit)

  def select(index: Int): Future[Boolean] =
    send(Select(index))

  def swapdb(index1: Int, index2: Int): Future[Boolean] =
    send(Swapdb(index1, index2))
}
