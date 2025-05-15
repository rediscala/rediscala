package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Strlen[K](key: K)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("STRLEN", Seq(keyAsString))
}
