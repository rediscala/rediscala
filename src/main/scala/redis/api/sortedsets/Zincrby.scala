package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Zincrby[K, V](key: K, increment: Double, member: V)(implicit keySeria: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandBulkDouble {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZINCRBY", Seq(keyAsString, ByteString(increment.toString), convert.serialize(member)))
}
