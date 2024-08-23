package redis.api.scripting

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.Bulk

case class ScriptLoad(script: String) extends RedisCommandBulk[String] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SCRIPT", Seq(ByteString("LOAD"), ByteString(script)))

  def decodeReply(bulk: Bulk) = bulk.toString
}
