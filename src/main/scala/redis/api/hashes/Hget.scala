package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*

case class Hget[K, KK, R](key: K, field: KK)(implicit
  redisKey: ByteStringSerializer[K],
  redisFields: ByteStringSerializer[KK],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HGET", Seq(keyAsString, redisFields.serialize(field)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
