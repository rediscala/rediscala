package redis.api.geo

import redis.RediscalaCompat.util.ByteString
import redis.*
import redis.api.geo.DistUnits.Measurement
import redis.protocol.*

case class GeoRadius[K](key: K, lat: Double, lng: Double, radius: Double, unit: Measurement)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode(
    "GEORADIUS",
    Seq(redisKey.serialize(key), ByteString(lng.toString), ByteString(lat.toString), ByteString(radius.toString), ByteString(unit.value))
  )
  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toStringsSeq(mb)
}
