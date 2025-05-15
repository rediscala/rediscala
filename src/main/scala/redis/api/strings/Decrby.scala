package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Decrby[K](key: K, decrement: Long)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("DECRBY", Seq(keyAsString, ByteString(decrement.toString)))
}
