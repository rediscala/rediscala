package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Bitcount[K](key: K)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("BITCOUNT", Seq(keyAsString))
}
