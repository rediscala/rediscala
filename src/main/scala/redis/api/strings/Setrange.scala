package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Setrange[K, V](key: K, offset: Long, value: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SETRANGE", Seq(keyAsString, ByteString(offset.toString), convert.serialize(value)))
}
