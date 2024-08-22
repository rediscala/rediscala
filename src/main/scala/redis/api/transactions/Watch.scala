package redis.api.transactions

import redis.RedisCommandStatusBoolean
import redis.RediscalaCompat.util.ByteString

case class Watch(keys: Set[String]) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("WATCH", keys.map(ByteString.apply).toSeq)
}
