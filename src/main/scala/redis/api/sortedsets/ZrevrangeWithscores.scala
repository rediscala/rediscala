package redis.api.sortedsets

import redis.RediscalaCompat.util.ByteString
import redis.*

case class ZrevrangeWithscores[K, R](key: K, start: Long, stop: Long)(implicit
  keySeria: ByteStringSerializer[K],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteStringDouble[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString =
    encode("ZREVRANGE", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString), ByteString("WITHSCORES")))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
