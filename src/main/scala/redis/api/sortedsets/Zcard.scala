package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Zcard[K](key: K)(implicit keySeria: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZCARD", Seq(keyAsString))
}
