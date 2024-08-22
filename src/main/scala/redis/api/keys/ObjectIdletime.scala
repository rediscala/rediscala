package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class ObjectIdletime[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandRedisReplyOptionLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("OBJECT", Seq(ByteString("IDLETIME"), keyAsString))
}
