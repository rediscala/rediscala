package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Flushall(async: Boolean = false) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("FLUSHALL", if (async) Seq(ByteString("ASYNC")) else Seq.empty)
}
