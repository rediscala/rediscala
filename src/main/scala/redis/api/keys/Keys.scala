package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.*

case class Keys(pattern: String) extends RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("KEYS", Seq(ByteString(pattern)))

  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toSeqString(mb)
}
