package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Type[K](key: K)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandStatusString {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("TYPE", Seq(keyAsString))
}
