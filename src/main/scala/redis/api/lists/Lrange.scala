package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk

case class Lrange[K, R](key: K, start: Long, stop: Long)(using ByteStringSerializer[K], ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[R]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("LRANGE", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString)))

  def decodeReply(mb: MultiBulk): Seq[R] = MultiBulkConverter.toSeqByteString(mb)
}
