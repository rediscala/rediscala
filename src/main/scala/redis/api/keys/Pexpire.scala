package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Pexpire[K](key: K, milliseconds: Long)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PEXPIRE", Seq(keyAsString, ByteString(milliseconds.toString)))
}
