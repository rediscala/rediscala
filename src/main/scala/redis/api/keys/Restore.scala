package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Restore[K, V](key: K, ttl: Long = 0, serializedValue: V)(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("RESTORE", Seq(keyAsString, ByteString(ttl.toString), convert.serialize(serializedValue)))
}
