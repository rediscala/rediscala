package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Rpush[K, V](key: K, values: Seq[V])(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("RPUSH", keyAsString +: values.map(convert.serialize))
}
