package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Psetex[K, V](key: K, milliseconds: Long, value: V)(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PSETEX", Seq(keyAsString, ByteString(milliseconds.toString), convert.serialize(value)))
}
