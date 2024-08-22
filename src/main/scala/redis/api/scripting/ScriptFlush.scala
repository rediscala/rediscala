package redis.api.scripting

import redis.*
import redis.RediscalaCompat.util.ByteString

case object ScriptFlush extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SCRIPT", Seq(ByteString("FLUSH")))
}
