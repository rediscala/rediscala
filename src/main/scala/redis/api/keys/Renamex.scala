package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Renamex[K, NK](key: K, newkey: NK)(implicit redisKey: ByteStringSerializer[K], newKeySer: ByteStringSerializer[NK])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("RENAMENX", Seq(keyAsString, newKeySer.serialize(newkey)))
}
