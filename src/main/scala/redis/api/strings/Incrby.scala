package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Incrby[K](key: K, increment: Long)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("INCRBY", Seq(keyAsString, ByteString(increment.toString)))
}
