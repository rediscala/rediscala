package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.ShutdownModifier

case class Shutdown(modifier: Option[ShutdownModifier] = None) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("SHUTDOWN", modifier.map(m => Seq(ByteString(m.toString))).getOrElse(Seq.empty))
}
