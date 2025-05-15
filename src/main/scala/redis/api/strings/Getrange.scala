package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Getrange[K, R](key: K, start: Long, end: Long)(using redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("GETRANGE", Seq(keyAsString, ByteString(start.toString), ByteString(end.toString)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
