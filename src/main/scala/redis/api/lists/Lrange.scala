package redis.api.lists

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.MultiBulk

case class Lrange[K, R](key: K, start: Long, stop: Long)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[R]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("LRANGE", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString)))

  def decodeReply(mb: MultiBulk) = MultiBulkConverter.toSeqByteString(mb)
}
