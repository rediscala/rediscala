package redis.api.geo

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.geo.DistUnits.Measurement
import redis.protocol.*

case class GeoDist[K](key: K, member1: String, member2: String, unit: Measurement)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandBulkDouble {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("GEODIST", Seq(redisKey.serialize(key), ByteString(member1), ByteString(member2), ByteString(unit.value)))

  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toStringsSeq(mb)
}
