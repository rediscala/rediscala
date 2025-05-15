package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Ltrim[K](key: K, start: Long, stop: Long)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("LTRIM", Seq(keyAsString, ByteString(start.toString), ByteString(stop.toString)))
}
