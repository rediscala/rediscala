package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Setrange[K, V](key: K, offset: Long, value: V)(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SETRANGE", Seq(keyAsString, ByteString(offset.toString), convert.serialize(value)))
}
