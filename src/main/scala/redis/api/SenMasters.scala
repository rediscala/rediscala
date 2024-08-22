package redis.api

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.MultiBulk

case class SenMasters() extends RedisCommandMultiBulk[Seq[Map[String, String]]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SENTINEL MASTERS")

  def decodeReply(mb: MultiBulk) = MultiBulkConverter.toSeqMapString(mb)
}
