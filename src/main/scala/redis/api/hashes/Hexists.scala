package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Hexists[K, KK](key: K, field: KK)(implicit redisKey: ByteStringSerializer[K], redisFields: ByteStringSerializer[KK])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HEXISTS", Seq(keyAsString, redisFields.serialize(field)))
}
