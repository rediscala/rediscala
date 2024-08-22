package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class ExistsMany[K](keys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends MultiClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("EXISTS", keys.map(redisKey.serialize))
}
