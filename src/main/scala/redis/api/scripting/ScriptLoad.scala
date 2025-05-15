package redis.api.scripting

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.Bulk

case class ScriptLoad(script: String) extends RedisCommandBulk[String] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SCRIPT", Seq(ByteString("LOAD"), ByteString(script)))

  def decodeReply(bulk: Bulk) = bulk.toString
}
