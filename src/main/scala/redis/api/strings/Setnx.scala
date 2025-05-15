package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Setnx[K, V](key: K, value: V)(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SETNX", Seq(keyAsString, convert.serialize(value)))
}
