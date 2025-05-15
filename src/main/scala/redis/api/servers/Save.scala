package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case object Save extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("SAVE")
}
