package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Lpush[K, V](key: K, values: Seq[V])(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("LPUSH", keyAsString +: values.map(convert.serialize))
}
