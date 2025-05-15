package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.Limit

case class Zremrangebyscore[K](key: K, min: Limit, max: Limit)(using ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZREMRANGEBYSCORE", Seq(keyAsString, min.toByteString, max.toByteString))
}
