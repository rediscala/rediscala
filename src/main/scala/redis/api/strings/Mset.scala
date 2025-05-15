package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*

case class Mset[K, V](keysValues: Map[K, V])(implicit redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends ClusterKey
    with RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(
    "MSET",
    keysValues.foldLeft(Seq[ByteString]()) { (acc, e) =>
      redisKey.serialize(e._1) +: convert.serialize(e._2) +: acc
    }
  )

  override def getSlot(): Int = MultiClusterKey.getHeadSlot(redisKey, keysValues.keys.toSeq)
}
