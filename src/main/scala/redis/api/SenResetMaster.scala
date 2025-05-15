package redis.api

import org.apache.pekko.util.ByteString
import redis.*

case class SenResetMaster(pattern: String) extends RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL RESET $pattern")
}
