package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*

case class Hlen[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HLEN", Seq(keyAsString))
}
