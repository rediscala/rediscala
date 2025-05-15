package redis

import org.apache.pekko.util.ByteString

abstract class SimpleClusterKey[K](using redisKey: ByteStringSerializer[K]) extends ClusterKey {
  val key: K
  val keyAsString: ByteString = redisKey.serialize(key)
  def getSlot(): Int = RedisComputeSlot.hashSlot(keyAsString.utf8String)
}
