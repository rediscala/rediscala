package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Psetex[K, V](key: K, milliseconds: Long, value: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PSETEX", Seq(keyAsString, ByteString(milliseconds.toString), convert.serialize(value)))
}
