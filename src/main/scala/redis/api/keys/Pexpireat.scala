package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Pexpireat[K](key: K, millisecondsTimestamp: Long)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PEXPIREAT", Seq(keyAsString, ByteString(millisecondsTimestamp.toString)))
}
