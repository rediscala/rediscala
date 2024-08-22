package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Expire[K](key: K, seconds: Long)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("EXPIRE", Seq(keyAsString, ByteString(seconds.toString)))
}
