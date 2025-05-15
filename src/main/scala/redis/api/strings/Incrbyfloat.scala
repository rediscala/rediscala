package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Incrbyfloat[K](key: K, increment: Double)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandBulkOptionDouble {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("INCRBYFLOAT", Seq(keyAsString, ByteString(increment.toString)))
}
