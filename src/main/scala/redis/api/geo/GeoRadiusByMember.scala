package redis.api.geo

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.geo.DistUnits.Measurement
import redis.protocol.*

case class GeoRadiusByMember[K](key: K, member: String, dist: Int, unit: Measurement)(using redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString =
    encode("GEORADIUSBYMEMBER", Seq(redisKey.serialize(key), ByteString(member), ByteString(dist.toString), ByteString(unit.value)))
  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toStringsSeq(mb)
}
