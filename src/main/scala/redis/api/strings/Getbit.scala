package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Getbit[K](key: K, offset: Long)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("GETBIT", Seq(keyAsString, ByteString(offset.toString)))
}
