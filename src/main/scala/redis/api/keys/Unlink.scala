package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Unlink[K](keys: Seq[K])(using redisKey: ByteStringSerializer[K]) extends MultiClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("UNLINK", keys.map(redisKey.serialize))
}
