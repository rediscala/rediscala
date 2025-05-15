package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Del[K](keys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends MultiClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("DEL", keys.map(redisKey.serialize))
}
