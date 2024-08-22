package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Incrby[K](key: K, increment: Long)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("INCRBY", Seq(keyAsString, ByteString(increment.toString)))
}
