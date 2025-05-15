package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*

case class Sunion[K, KK, R](key: K, keys: Seq[KK])(using
  redisKey: ByteStringSerializer[K],
  redisKeys: ByteStringSerializer[KK],
  deserializerR: ByteStringDeserializer[R]
) extends RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SUNION", redisKey.serialize(key) +: keys.map(redisKeys.serialize))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
