package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString

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
