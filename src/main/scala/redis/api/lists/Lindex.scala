package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Lindex[K, R](key: K, index: Long)(using redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("LINDEX", Seq(keyAsString, ByteString(index.toString)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
