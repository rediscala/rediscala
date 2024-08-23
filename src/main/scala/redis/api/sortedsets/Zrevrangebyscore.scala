package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.Limit

case class Zrevrangebyscore[K: ByteStringSerializer, R](key: K, min: Limit, max: Limit, limit: Option[(Long, Long)] = None)(implicit
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZREVRANGEBYSCORE", Zrevrangebyscore.buildArgs(key, min, max, withscores = false, limit))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}

private[redis] object Zrevrangebyscore {
  def buildArgs[K](key: K, min: Limit, max: Limit, withscores: Boolean, limit: Option[(Long, Long)])(implicit
    keySeria: ByteStringSerializer[K]
  ): Seq[ByteString] = {
    /*
     * Find the actual min/max and reverse them in order to support backwards compatibility and legacy clients.
     * See discussion in [[https://github.com/etaty/rediscala/issues/98 Github Issue]].
     */
    val (_min, _max) = if (min.value < max.value) min -> max else max -> min
    Zrangebyscore.buildArgs(key, _max, _min, withscores, limit)
  }
}
