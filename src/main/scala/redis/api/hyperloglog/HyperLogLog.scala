package redis.api.hyperloglog

import redis.RediscalaCompat.util.ByteString
import redis.RedisCommandIntegerLong
import redis.RedisCommandStatusBoolean
import redis.ByteStringSerializer

case class Pfadd[K, V](key: K, values: Seq[V])(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PFADD", redisKey.serialize(key) +: values.map(convert.serialize))
}

case class Pfcount[K](keys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("PFCOUNT", keys.map(redisKey.serialize))
}

case class Pfmerge[K](destKey: K, sourceKeys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PFMERGE", redisKey.serialize(destKey) +: sourceKeys.map(redisKey.serialize))
}
