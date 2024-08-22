package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case object Bgsave extends RedisCommandStatusString {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("BGSAVE")
}
