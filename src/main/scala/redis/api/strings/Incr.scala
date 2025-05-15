package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Incr[K](key: K)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("INCR", Seq(keyAsString))
}
