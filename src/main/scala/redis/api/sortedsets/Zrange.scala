package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zrange[K, R](key: K, start: Long, stop: Long)(using keySeria: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  val encodedRequest: ByteString = encode("ZRANGE", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString)))
  def isMasterOnly = false
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
