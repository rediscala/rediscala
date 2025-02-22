package redis.api.geo

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.*

case class GeoPos[K](key: K, member: Seq[String])(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val members: Seq[ByteString] = member.foldLeft(Seq.empty[ByteString]) { (acc, e) => ByteString(e) +: acc }
  val keySec: Seq[ByteString] = Seq(redisKey.serialize(key))
  val encodedRequest: ByteString = encode("GEOPOS", keySec ++ members)
  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toStringsSeq(mb)
}
