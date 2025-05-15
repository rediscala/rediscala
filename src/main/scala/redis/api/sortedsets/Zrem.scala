package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zrem[K, V](key: K, members: Seq[V])(using keySeria: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZREM", keyAsString +: members.map(convert.serialize))
}
