package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Lset[K, V](key: K, index: Long, value: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("LSET", Seq(keyAsString, ByteString(index.toString), convert.serialize(value)))
}
