package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.Limit

case class ZrevrangebyscoreWithscores[K: ByteStringSerializer, R](key: K, min: Limit, max: Limit, limit: Option[(Long, Long)] = None)(implicit
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteStringDouble[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZREVRANGEBYSCORE", Zrevrangebyscore.buildArgs(key, min, max, withscores = true, limit))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
