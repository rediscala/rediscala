package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class ZrangeWithscores[K, R](key: K, start: Long, stop: Long)(using
  keySeria: ByteStringSerializer[K],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteStringDouble[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZRANGE", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString), ByteString("WITHSCORES")))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
