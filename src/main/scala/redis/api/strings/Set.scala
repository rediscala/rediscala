package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.RedisProtocolRequest
import redis.protocol.RedisReply
import redis.protocol.Status

case class Set[K, V](key: K, value: V, exSeconds: Option[Long] = None, pxMilliseconds: Option[Long] = None, NX: Boolean = false, XX: Boolean = false)(
  implicit
  redisKey: ByteStringSerializer[K],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandRedisReply[Boolean] {
  def isMasterOnly = true
  val encodedRequest: ByteString = {
    val builder = Seq.newBuilder[ByteString]

    builder.+=(redisKey.serialize(key))
    builder.+=(convert.serialize(value))

    if (NX)
      builder += ByteString("NX")
    else if (XX)
      builder += ByteString("XX")

    if (exSeconds.isDefined) {
      builder += ByteString("EX")
      builder += ByteString(exSeconds.get.toString)
    } else if (pxMilliseconds.isDefined) {
      builder += ByteString("PX")
      builder += ByteString(pxMilliseconds.get.toString)
    }

    RedisProtocolRequest.multiBulk("SET", builder.result())
  }

  def decodeReply(redisReply: RedisReply) = redisReply match {
    case s: Status => s.toBoolean
    case _ => false
  }
}
