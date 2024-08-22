package redis.api.sortedsets

import redis.RediscalaCompat.util.ByteString
import redis.*

case class Zscore[K, V](key: K, member: V)(implicit keySeria: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionDouble {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZSCORE", Seq(keyAsString, convert.serialize(member)))
}
