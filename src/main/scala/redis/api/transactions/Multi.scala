package redis.api.transactions

import redis.RedisCommandStatusBoolean
import redis.RediscalaCompat.util.ByteString

case object Multi extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("MULTI")
}
