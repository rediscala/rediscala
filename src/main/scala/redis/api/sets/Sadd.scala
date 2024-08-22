package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Sadd[K, V](key: K, members: Seq[V])(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SADD", keyAsString +: members.map(v => convert.serialize(v)))
}
