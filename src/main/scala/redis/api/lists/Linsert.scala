package redis.api.lists

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.ListPivot

case class Linsert[K, KP, V](key: K, beforeAfter: ListPivot, pivot: KP, value: V)(implicit
  redisKey: ByteStringSerializer[K],
  redisPivot: ByteStringSerializer[KP],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString =
    encode("LINSERT", Seq(keyAsString, ByteString(beforeAfter.toString), redisPivot.serialize(pivot), convert.serialize(value)))
}
