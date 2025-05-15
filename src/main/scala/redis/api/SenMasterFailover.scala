package redis.api

import org.apache.pekko.util.ByteString
import redis.*

case class SenMasterFailover(master: String) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL FAILOVER $master")
}
