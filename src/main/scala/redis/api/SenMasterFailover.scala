package redis.api

import redis.*
import redis.RediscalaCompat.util.ByteString

case class SenMasterFailover(master: String) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL FAILOVER $master")
}
