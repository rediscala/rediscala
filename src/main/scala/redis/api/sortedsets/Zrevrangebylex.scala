package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zrevrangebylex[K, R](key: K, max: String, min: String, limit: Option[(Long, Long)] = None)(using
  keySeria: ByteStringSerializer[K],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZREVRANGEBYLEX", Zrangebylex.buildArgs(keyAsString, max, min, limit))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
