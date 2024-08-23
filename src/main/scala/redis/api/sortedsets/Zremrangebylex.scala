package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Zremrangebylex[K](key: K, min: String, max: String)(implicit keySeria: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZREMRANGEBYLEX", Seq(keyAsString, ByteString(min), ByteString(max)))
}
