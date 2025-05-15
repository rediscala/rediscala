package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Rename[K, NK](key: K, newkey: NK)(implicit redisKey: ByteStringSerializer[K], newKeySer: ByteStringSerializer[NK])
    extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("RENAME", Seq(keyAsString, newKeySer.serialize(newkey)))
}
