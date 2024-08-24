package redis.commands

import redis.Request
import redis.api.geo.*
import redis.api.geo.DistUnits.Kilometer
import redis.api.geo.DistUnits.Measurement
import redis.api.geo.DistUnits.Meter
import redis.api.geo.GeoOptions.WithDist
import redis.api.geo.GeoOptions.WithOption
import scala.concurrent.Future

trait Geo extends Request {

  def geoAdd(key: String, lat: Double, lng: Double, loc: String): Future[Long] =
    send(GeoAdd(key, lat, lng, loc))

  def geoRadius(key: String, lat: Double, lng: Double, radius: Double, dim: Measurement = Kilometer): Future[Seq[String]] =
    send(GeoRadius(key, lat, lng, radius, dim))

  def geoRadiusByMember(key: String, member: String, dist: Int, dim: Measurement = Meter): Future[Seq[String]] =
    send(GeoRadiusByMember(key, member, dist, dim))

  def geoRadiusByMemberWithOpt(
    key: String,
    member: String,
    dist: Int,
    dim: Measurement = Meter,
    opt: WithOption = WithDist,
    count: Int = Int.MaxValue
  ): Future[Seq[String]] =
    send(GeoRadiusByMemberWithOpt(key, member, dist, dim, opt, count))

  def geoDist(key: String, member1: String, member2: String, unit: Measurement = Meter): Future[Double] =
    send(GeoDist(key, member1, member2, unit))

  def geoHash(key: String, members: String*): Future[Seq[String]] = send(GeoHash(key, members))

  def geoPos(key: String, members: String*): Future[Seq[String]] = send(GeoPos(key, members))

}
