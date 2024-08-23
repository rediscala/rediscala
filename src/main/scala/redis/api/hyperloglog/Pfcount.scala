package redis.api.hyperloglog

import redis.ByteStringSerializer
import redis.RedisCommandIntegerLong
import redis.RediscalaCompat.util.ByteString

case class Pfcount[K](keys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("PFCOUNT", keys.map(redisKey.serialize))
}
