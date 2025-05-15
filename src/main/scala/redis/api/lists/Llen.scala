package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Llen[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("LLEN", Seq(keyAsString))
}
