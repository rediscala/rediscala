package redis.api.connection

import org.apache.pekko.util.ByteString
import redis.*

case object Ping extends RedisCommandStatusString {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PING")
}
