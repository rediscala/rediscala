package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Pttl[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("PTTL", Seq(keyAsString))
}
