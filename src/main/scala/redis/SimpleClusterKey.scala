package redis

import redis.RediscalaCompat.util.ByteString

abstract class SimpleClusterKey[K](implicit redisKey: ByteStringSerializer[K]) extends ClusterKey {
  val key: K
  val keyAsString: ByteString = redisKey.serialize(key)
  def getSlot(): Int = RedisComputeSlot.hashSlot(keyAsString.utf8String)
}
