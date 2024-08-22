package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Bitcount[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("BITCOUNT", Seq(keyAsString))
}
