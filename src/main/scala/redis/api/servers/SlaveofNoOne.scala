package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case object SlaveofNoOne extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("SLAVEOF NO ONE")
}
