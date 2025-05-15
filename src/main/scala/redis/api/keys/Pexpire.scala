package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Pexpire[K](key: K, milliseconds: Long)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PEXPIRE", Seq(keyAsString, ByteString(milliseconds.toString)))
}
