package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Persist[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PERSIST", Seq(keyAsString))
}
