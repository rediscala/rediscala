package redis.api.lists

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Rpushx[K, V](key: K, values: Seq[V])(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("RPUSHX", keyAsString +: values.map(convert.serialize))
}
