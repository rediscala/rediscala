package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class ObjectIdletime[K](key: K)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandRedisReplyOptionLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("OBJECT", Seq(ByteString("IDLETIME"), keyAsString))
}
