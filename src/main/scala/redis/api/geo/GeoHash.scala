package redis.api.geo

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.*

case class GeoHash[K](key: K, member: Seq[String])(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val members: Seq[ByteString] = member.foldLeft(Seq.empty[ByteString]) { (acc, e) => ByteString(e) +: acc }
  val keySec: Seq[ByteString] = Seq(redisKey.serialize(key))
  val encodedRequest: ByteString = encode("GEOHASH", keySec ++ members)
  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toStringsSeq(mb)
}
