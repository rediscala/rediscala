package redis.api.lists

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Lindex[K, R](key: K, index: Long)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("LINDEX", Seq(keyAsString, ByteString(index.toString)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
