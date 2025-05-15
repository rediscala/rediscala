package redis.api.hyperloglog

import org.apache.pekko.util.ByteString
import redis.ByteStringSerializer
import redis.RedisCommandIntegerLong

case class Pfcount[K](keys: Seq[K])(using redisKey: ByteStringSerializer[K]) extends RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("PFCOUNT", keys.map(redisKey.serialize))
}
