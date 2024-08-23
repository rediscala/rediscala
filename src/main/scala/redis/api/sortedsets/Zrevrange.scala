package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Zrevrange[K, R](key: K, start: Long, stop: Long)(implicit keySeria: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZREVRANGE", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
