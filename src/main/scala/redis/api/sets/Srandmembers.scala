package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Srandmembers[K, R](key: K, count: Long)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SRANDMEMBER", Seq(keyAsString, ByteString(count.toString)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
