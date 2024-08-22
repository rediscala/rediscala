package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Sismember[K, V](key: K, member: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SISMEMBER", Seq(keyAsString, convert.serialize(member)))
}
