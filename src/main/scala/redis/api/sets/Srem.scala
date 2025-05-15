package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*

case class Srem[K, V](key: K, members: Seq[V])(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SREM", keyAsString +: members.map(convert.serialize))
}
