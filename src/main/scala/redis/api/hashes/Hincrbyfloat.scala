package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Hincrbyfloat[K, KK](key: K, fields: KK, increment: Double)(implicit
  redisKey: ByteStringSerializer[K],
  redisFields: ByteStringSerializer[KK]
) extends SimpleClusterKey[K]
    with RedisCommandBulkDouble {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("HINCRBYFLOAT", Seq(keyAsString, redisFields.serialize(fields), ByteString(increment.toString)))
}
