package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Srandmember[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SRANDMEMBER", Seq(keyAsString))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
