package redis.api.sortedsets

import redis.RediscalaCompat.util.ByteString
import redis.*

case class Zrem[K, V](key: K, members: Seq[V])(implicit keySeria: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZREM", keyAsString +: members.map(v => convert.serialize(v)))
}
