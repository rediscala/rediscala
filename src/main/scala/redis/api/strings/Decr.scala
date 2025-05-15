package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Decr[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("DECR", Seq(keyAsString))
}
