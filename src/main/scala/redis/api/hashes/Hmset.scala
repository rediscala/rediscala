package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*

case class Hmset[K, KK, V](key: K, keysValues: Map[KK, V])(using
  redisKey: ByteStringSerializer[K],
  redisFields: ByteStringSerializer[KK],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(
    "HMSET",
    keyAsString +: keysValues.foldLeft(Seq.empty[ByteString]) { (acc, e) =>
      redisFields.serialize(e._1) +: convert.serialize(e._2) +: acc
    }
  )
}
