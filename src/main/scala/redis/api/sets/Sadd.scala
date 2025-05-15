package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*

case class Sadd[K, V](key: K, members: Seq[V])(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SADD", keyAsString +: members.map(convert.serialize))
}
