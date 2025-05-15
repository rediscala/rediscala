package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Randomkey[R]()(using deserializerR: ByteStringDeserializer[R]) extends RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("RANDOMKEY")
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
