package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zrevrank[K, V](key: K, member: V)(using keySeria: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandRedisReplyOptionLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZREVRANK", Seq(keyAsString, convert.serialize(member)))
}
