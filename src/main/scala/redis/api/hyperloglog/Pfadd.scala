package redis.api.hyperloglog

import redis.RediscalaCompat.util.ByteString
import redis.RedisCommandIntegerLong
import redis.ByteStringSerializer

case class Pfadd[K, V](key: K, values: Seq[V])(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PFADD", redisKey.serialize(key) +: values.map(convert.serialize))
}
