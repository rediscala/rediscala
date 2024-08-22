package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.Bulk

case class Info(section: Option[String] = None) extends RedisCommandBulk[String] {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("INFO", section.map(s => Seq(ByteString(s))).getOrElse(Seq()))

  def decodeReply(r: Bulk): String = r.toOptString.get
}
