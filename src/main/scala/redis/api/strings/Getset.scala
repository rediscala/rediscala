package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Getset[K, V, R](key: K, value: V)(implicit
  redisKey: ByteStringSerializer[K],
  convert: ByteStringSerializer[V],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("GETSET", Seq(keyAsString, convert.serialize(value)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
