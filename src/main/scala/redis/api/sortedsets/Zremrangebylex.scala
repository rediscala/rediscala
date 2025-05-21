package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zremrangebylex[K](key: K, min: String, max: String)(using ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZREMRANGEBYLEX", Seq(keyAsString, ByteString(min), ByteString(max)))
}
