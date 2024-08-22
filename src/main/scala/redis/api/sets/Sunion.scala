package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Sunion[K, KK, R](key: K, keys: Seq[KK])(implicit
  redisKey: ByteStringSerializer[K],
  redisKeys: ByteStringSerializer[KK],
  deserializerR: ByteStringDeserializer[R]
) extends RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SUNION", redisKey.serialize(key) +: keys.map(redisKeys.serialize))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
