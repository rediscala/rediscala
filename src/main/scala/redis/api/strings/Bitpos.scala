package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Bitpos[K](key: K, bit: Long, start: Long = 0, end: Long = -1)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("BITPOS", Seq(keyAsString, ByteString(bit.toString), ByteString(start.toString), ByteString(end.toString)))
}
