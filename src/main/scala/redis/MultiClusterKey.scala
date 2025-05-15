package redis

abstract class MultiClusterKey[K](using redisKey: ByteStringSerializer[K]) extends ClusterKey {
  val keys: Seq[K]
  def getSlot(): Int = MultiClusterKey.getHeadSlot(redisKey, keys)
}

object MultiClusterKey {
  def getHeadSlot[K](redisKey: ByteStringSerializer[K], keys: Seq[K]): Int = {
    RedisComputeSlot.hashSlot(redisKey.serialize(keys.headOption.getOrElse(throw new RuntimeException("operation has not keys"))).utf8String)
  }
}
