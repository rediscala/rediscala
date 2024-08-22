package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case object ConfigResetstat extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CONFIG", Seq(ByteString("RESETSTAT")))
}
