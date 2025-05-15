package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Lpop[K, R](key: K)(using redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("LPOP", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
