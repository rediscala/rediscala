package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*

case class Sismember[K, V](key: K, member: V)(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SISMEMBER", Seq(keyAsString, convert.serialize(member)))
}
