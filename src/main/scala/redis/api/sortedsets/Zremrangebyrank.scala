package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Zremrangebyrank[K](key: K, start: Long, stop: Long)(implicit keySeria: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZREMRANGEBYRANK", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString)))
}
