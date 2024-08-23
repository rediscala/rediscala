package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class ZrangeWithscores[K, R](key: K, start: Long, stop: Long)(implicit
  keySeria: ByteStringSerializer[K],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteStringDouble[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZRANGE", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString), ByteString("WITHSCORES")))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
