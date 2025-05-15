package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case object DebugSegfault extends RedisCommandStatusString {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("DEBUG SEGFAULT")
}
