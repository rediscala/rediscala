package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Append[K, V](key: K, value: V)(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("APPEND", Seq(keyAsString, convert.serialize(value)))
}
