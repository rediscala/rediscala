package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Hincrby[K, KK](key: K, fields: KK, increment: Long)(implicit redisKey: ByteStringSerializer[K], redisFields: ByteStringSerializer[KK])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("HINCRBY", Seq(keyAsString, redisFields.serialize(fields), ByteString(increment.toString)))
}
