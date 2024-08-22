package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class BitcountRange[K](key: K, start: Long, end: Long)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("BITCOUNT", Seq(keyAsString, ByteString(start.toString), ByteString(end.toString)))
}
