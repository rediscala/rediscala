package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Randomkey[R]()(implicit deserializerR: ByteStringDeserializer[R]) extends RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("RANDOMKEY")
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
