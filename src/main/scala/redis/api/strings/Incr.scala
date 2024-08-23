package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Incr[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("INCR", Seq(keyAsString))
}