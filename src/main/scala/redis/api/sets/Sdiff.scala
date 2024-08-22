package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Sdiff[K, KK, R](key: K, keys: Seq[KK])(implicit
  redisKey: ByteStringSerializer[K],
  redisKeys: ByteStringSerializer[KK],
  deserializerR: ByteStringDeserializer[R]
) extends RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SDIFF", redisKey.serialize(key) +: keys.map(redisKeys.serialize))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
