package redis.api.geo

import redis.*
import redis.RediscalaCompat.util.ByteString

case class GeoAdd[K](key: K, lat: Double, lng: Double, loc: String)(implicit redisKey: ByteStringSerializer[K])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("GEOADD", Seq(redisKey.serialize(key), ByteString(lng.toString), ByteString(lat.toString), ByteString(loc)))
}
