package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Setex[K, V](key: K, seconds: Long, value: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SETEX", Seq(keyAsString, ByteString(seconds.toString), convert.serialize(value)))
}
