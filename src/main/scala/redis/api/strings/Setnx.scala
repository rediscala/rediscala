package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Setnx[K, V](key: K, value: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SETNX", Seq(keyAsString, convert.serialize(value)))
}
