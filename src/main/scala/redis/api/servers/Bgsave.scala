package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case object Bgsave extends RedisCommandStatusString {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("BGSAVE")
}
