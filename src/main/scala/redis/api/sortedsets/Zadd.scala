package redis.api.sortedsets

import redis.RediscalaCompat.util.ByteString
import redis.*
import redis.api.ZaddOption

case class Zadd[K, V](key: K, options: Seq[ZaddOption], scoreMembers: Seq[(Double, V)])(implicit
  keySeria: ByteStringSerializer[K],
  convert: ByteStringSerializer[V]
) extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(
    "ZADD",
    keyAsString +: (options.map(_.serialize) ++
      scoreMembers.foldLeft(Seq.empty[ByteString]) { case (acc, e) =>
        ByteString(e._1.toString) +: convert.serialize(e._2) +: acc
      })
  )
}
