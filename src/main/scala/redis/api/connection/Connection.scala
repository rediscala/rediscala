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

case class Echo[V, R](value: V)(implicit convert: ByteStringSerializer[V], deserializerR: ByteStringDeserializer[R])
    extends RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ECHO", Seq(convert.serialize(value)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}

case object Ping extends RedisCommandStatusString {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PING")
}

case object Quit extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("QUIT")
}

case class Select(index: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SELECT", Seq(ByteString(index.toString)))
}

case class Swapdb(index1: Int, index2: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SWAPDB", Seq(ByteString(index1.toString), ByteString(index2.toString)))
}
