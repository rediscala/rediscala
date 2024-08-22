package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Smembers[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SMEMBERS", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
