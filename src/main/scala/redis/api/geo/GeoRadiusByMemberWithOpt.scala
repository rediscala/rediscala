package redis.api.geo

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.geo.DistUnits.Measurement
import redis.api.geo.GeoOptions.WithOption
import redis.protocol.*

case class GeoRadiusByMemberWithOpt[K](key: K, member: String, dist: Int, unit: Measurement, opt: WithOption, count: Int)(using
  redisKey: ByteStringSerializer[K]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode(
    "GEORADIUSBYMEMBER",
    Seq(
      redisKey.serialize(key),
      ByteString(member),
      ByteString(dist.toString),
      ByteString(unit.value),
      ByteString(opt.value),
      ByteString("COUNT"),
      ByteString(count.toString)
    )
  )
  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toStringsSeq(mb)

}
