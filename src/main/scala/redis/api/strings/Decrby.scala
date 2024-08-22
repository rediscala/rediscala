package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Decrby[K](key: K, decrement: Long)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("DECRBY", Seq(keyAsString, ByteString(decrement.toString)))
}
