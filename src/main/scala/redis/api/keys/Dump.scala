package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Dump[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("DUMP", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
