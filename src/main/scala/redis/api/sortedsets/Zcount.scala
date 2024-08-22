package redis.api.sortedsets

import redis.RediscalaCompat.util.ByteString
import redis.*
import redis.api.Limit

case class Zcount[K](key: K, min: Limit = Limit(Double.NegativeInfinity), max: Limit = Limit(Double.PositiveInfinity))(implicit
  keySeria: ByteStringSerializer[K]
) extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZCOUNT", Seq(keyAsString, min.toByteString, max.toByteString))
}
