package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.MultiBulk

case class Hkeys[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandMultiBulk[Seq[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HKEYS", Seq(keyAsString))

  def decodeReply(mb: MultiBulk): Seq[String] = MultiBulkConverter.toSeqString(mb)
}
