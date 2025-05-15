package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Pttl[K](key: K)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("PTTL", Seq(keyAsString))
}
