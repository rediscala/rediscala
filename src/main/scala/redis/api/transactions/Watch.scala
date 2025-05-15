package redis.api.transactions

import org.apache.pekko.util.ByteString
import redis.RedisCommandStatusBoolean

case class Watch(keys: Set[String]) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("WATCH", keys.map(ByteString.apply).toSeq)
}
