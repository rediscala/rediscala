package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Exists[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("EXISTS", Seq(keyAsString))
}
