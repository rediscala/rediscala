package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Unlink[K](keys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends MultiClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("UNLINK", keys.map(redisKey.serialize))
}
