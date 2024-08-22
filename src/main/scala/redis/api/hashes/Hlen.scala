package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Hlen[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HLEN", Seq(keyAsString))
}
