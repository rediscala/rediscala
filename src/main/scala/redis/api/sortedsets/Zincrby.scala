package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zincrby[K, V](key: K, increment: Double, member: V)(using keySeria: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandBulkDouble {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZINCRBY", Seq(keyAsString, ByteString(increment.toString), convert.serialize(member)))
}
