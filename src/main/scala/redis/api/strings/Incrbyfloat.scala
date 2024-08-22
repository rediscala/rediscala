package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Incrbyfloat[K](key: K, increment: Double)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandBulkOptionDouble {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("INCRBYFLOAT", Seq(keyAsString, ByteString(increment.toString)))
}
