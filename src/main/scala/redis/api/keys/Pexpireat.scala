package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Pexpireat[K](key: K, millisecondsTimestamp: Long)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PEXPIREAT", Seq(keyAsString, ByteString(millisecondsTimestamp.toString)))
}
