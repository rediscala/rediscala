package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Hset[K, KK, V](key: K, keysValues: Map[KK, V])(implicit
  redisKey: ByteStringSerializer[K],
  redisFields: ByteStringSerializer[KK],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(
    "HSET",
    keyAsString :: keysValues.foldLeft(List.empty[ByteString]) { case (acc, (k, v)) =>
      redisFields.serialize(k) :: convert.serialize(v) :: acc
    }
  )
}
