package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*

case class Scard[K](key: K)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SCARD", Seq(keyAsString))
}
