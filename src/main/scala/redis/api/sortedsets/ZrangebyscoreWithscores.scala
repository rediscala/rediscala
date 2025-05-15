package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.Limit

case class ZrangebyscoreWithscores[K: ByteStringSerializer, R](key: K, min: Limit, max: Limit, limit: Option[(Long, Long)] = None)(using
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteStringDouble[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZRANGEBYSCORE", Zrangebyscore.buildArgs(key, min, max, withscores = true, limit))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
