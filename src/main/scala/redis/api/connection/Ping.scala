package redis.api.connection

import redis.*
import redis.RediscalaCompat.util.ByteString

case object Ping extends RedisCommandStatusString {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PING")
}
