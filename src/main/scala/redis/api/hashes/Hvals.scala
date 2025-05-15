package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*

case class Hvals[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HVALS", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
