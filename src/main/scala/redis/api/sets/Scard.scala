package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Scard[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SCARD", Seq(keyAsString))
}
