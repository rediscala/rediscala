package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Flushdb(async: Boolean = false) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("FLUSHDB", if (async) Seq(ByteString("ASYNC")) else Seq.empty)
}
