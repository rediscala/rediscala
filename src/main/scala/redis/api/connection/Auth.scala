package redis.api.connection

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.Status

case class Auth[V](value1: V, value2: Option[V] = None)(implicit convert: ByteStringSerializer[V]) extends RedisCommandStatus[Status] {
  def isMasterOnly = true
  val encodedRequest: ByteString = (value1, value2) match {
    case (username, Some(password)) => encode("AUTH", Seq(convert.serialize(username), convert.serialize(password)))
    case (password, None) => encode("AUTH", Seq(convert.serialize(password)))
    case (_, _) => ByteString.empty
  }

  def decodeReply(s: Status) = s
}
