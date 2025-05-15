package redis.api.transactions

import org.apache.pekko.util.ByteString
import redis.RedisCommandStatusBoolean

case object Multi extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("MULTI")
}
