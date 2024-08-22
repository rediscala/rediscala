package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Append[K, V](key: K, value: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("APPEND", Seq(keyAsString, convert.serialize(value)))
}
