package redis.api

import redis.*
import redis.RediscalaCompat.util.ByteString

case class SenResetMaster(pattern: String) extends RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL RESET $pattern")
}
