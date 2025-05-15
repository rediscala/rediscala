package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk

case class Hkeys[K](key: K)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HKEYS", Seq(keyAsString))

  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toSeqString(mb)
}
