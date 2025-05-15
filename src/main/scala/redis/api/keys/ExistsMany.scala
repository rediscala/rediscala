package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class ExistsMany[K](keys: Seq[K])(using redisKey: ByteStringSerializer[K]) extends MultiClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("EXISTS", keys.map(redisKey.serialize))
}
