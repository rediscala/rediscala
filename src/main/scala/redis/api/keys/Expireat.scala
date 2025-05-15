package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Expireat[K](key: K, seconds: Long)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("EXPIREAT", Seq(keyAsString, ByteString(seconds.toString)))
}
