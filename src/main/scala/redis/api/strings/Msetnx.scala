package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Msetnx[K, V](keysValues: Map[K, V])(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(
    "MSETNX",
    keysValues.foldLeft(Seq[ByteString]()) { (acc, e) =>
      redisKey.serialize(e._1) +: convert.serialize(e._2) +: acc
    }
  )
}
