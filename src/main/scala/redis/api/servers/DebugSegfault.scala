package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case object DebugSegfault extends RedisCommandStatusString {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("DEBUG SEGFAULT")
}
