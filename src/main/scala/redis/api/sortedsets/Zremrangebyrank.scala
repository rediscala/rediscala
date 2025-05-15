package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zremrangebyrank[K](key: K, start: Long, stop: Long)(using ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZREMRANGEBYRANK", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString)))
}
