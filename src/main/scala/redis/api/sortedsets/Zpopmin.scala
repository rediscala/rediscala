package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zpopmin[K, R](key: K, count: Long)(using
  keySeria: ByteStringSerializer[K],
  countSeria: ByteStringSerializer[Long],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZPOPMIN", Seq(keyAsString, countSeria.serialize(count)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
