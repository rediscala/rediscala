package redis.api.scripting

import org.apache.pekko.util.ByteString
import redis.*

case object ScriptKill extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SCRIPT", Seq(ByteString("KILL")))
}
