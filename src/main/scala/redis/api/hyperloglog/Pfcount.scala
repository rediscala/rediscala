package redis.api.hyperloglog

import redis.RediscalaCompat.util.ByteString
import redis.RedisCommandIntegerLong
import redis.ByteStringSerializer

case class Pfcount[K](keys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("PFCOUNT", keys.map(redisKey.serialize))
}
