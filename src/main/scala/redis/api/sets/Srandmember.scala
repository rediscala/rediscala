package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*

case class Srandmember[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SRANDMEMBER", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
