package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Hmset[K, KK, V](key: K, keysValues: Map[KK, V])(implicit
  redisKey: ByteStringSerializer[K],
  redisFields: ByteStringSerializer[KK],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(
    "HMSET",
    keyAsString +: keysValues.foldLeft(Seq.empty[ByteString]) { case (acc, e) =>
      redisFields.serialize(e._1) +: convert.serialize(e._2) +: acc
    }
  )
}
