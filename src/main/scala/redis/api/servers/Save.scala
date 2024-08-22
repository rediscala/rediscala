package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case object Save extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("SAVE")
}
