package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*

case class Hsetnx[K, KK, V](key: K, field: KK, value: V)(using
  redisKey: ByteStringSerializer[K],
  redisFields: ByteStringSerializer[KK],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("HSETNX", Seq(keyAsString, redisFields.serialize(field), convert.serialize(value)))
}
