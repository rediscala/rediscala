package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case class Flushall(async: Boolean = false) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("FLUSHALL", if (async) Seq(ByteString("ASYNC")) else Seq.empty)
}
