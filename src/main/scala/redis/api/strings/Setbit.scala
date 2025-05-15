package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Setbit[K](key: K, offset: Long, value: Boolean)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SETBIT", Seq(keyAsString, ByteString(offset.toString), ByteString(if (value) "1" else "0")))
}
