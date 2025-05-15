package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Rpop[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("RPOP", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR

}
