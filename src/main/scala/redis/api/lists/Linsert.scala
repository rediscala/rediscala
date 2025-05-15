package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.ListPivot

case class Linsert[K, KP, V](key: K, beforeAfter: ListPivot, pivot: KP, value: V)(using
  redisKey: ByteStringSerializer[K],
  redisPivot: ByteStringSerializer[KP],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString =
    encode("LINSERT", Seq(keyAsString, ByteString(beforeAfter.toString), redisPivot.serialize(pivot), convert.serialize(value)))
}
