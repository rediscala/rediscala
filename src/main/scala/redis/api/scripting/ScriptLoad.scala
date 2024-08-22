package redis.api.scripting

import redis.protocol.Bulk
import redis.*
import redis.RediscalaCompat.util.ByteString

case class ScriptLoad(script: String) extends RedisCommandBulk[String] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SCRIPT", Seq(ByteString("LOAD"), ByteString(script)))

  def decodeReply(bulk: Bulk) = bulk.toString
}
