package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Spop[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SPOP", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
