package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Getbit[K](key: K, offset: Long)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("GETBIT", Seq(keyAsString, ByteString(offset.toString)))
}
