package redis.api.lists

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Lrem[K, V](key: K, count: Long, value: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("LREM", Seq(keyAsString, ByteString(count.toString), convert.serialize(value)))
}
