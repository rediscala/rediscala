package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Hsetnx[K, KK, V](key: K, field: KK, value: V)(implicit
  redisKey: ByteStringSerializer[K],
  redisFields: ByteStringSerializer[KK],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("HSETNX", Seq(keyAsString, redisFields.serialize(field), convert.serialize(value)))
}
