package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Zscore[K, V](key: K, member: V)(implicit keySeria: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionDouble {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZSCORE", Seq(keyAsString, convert.serialize(member)))
}
