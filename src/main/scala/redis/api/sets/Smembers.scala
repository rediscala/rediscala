package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*

case class Smembers[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SMEMBERS", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
