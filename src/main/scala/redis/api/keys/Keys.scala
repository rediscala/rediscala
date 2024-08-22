package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.*

case class Keys(pattern: String) extends RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("KEYS", Seq(ByteString(pattern)))

  def decodeReply(mb: MultiBulk) = MultiBulkConverter.toSeqString(mb)
}
