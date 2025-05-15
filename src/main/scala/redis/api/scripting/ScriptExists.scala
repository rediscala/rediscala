package redis.api.scripting

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk

case class ScriptExists(sha1: Seq[String]) extends RedisCommandMultiBulk[Seq[Boolean]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SCRIPT", ByteString("EXISTS") +: sha1.map(ByteString(_)))

  def decodeReply(mb: MultiBulk): Seq[Boolean] = MultiBulkConverter.toSeqBoolean(mb)
}
