package redis.api.connection

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.Status

case class Auth[V](value1: V, value2: Option[V] = None)(using convert: ByteStringSerializer[V]) extends RedisCommandStatus[Status] {
  def isMasterOnly = true
  val encodedRequest: ByteString = (value1, value2) match {
    case (username, Some(password)) => encode("AUTH", Seq(convert.serialize(username), convert.serialize(password)))
    case (password, None) => encode("AUTH", Seq(convert.serialize(password)))
  }

  def decodeReply(s: Status) = s
}
