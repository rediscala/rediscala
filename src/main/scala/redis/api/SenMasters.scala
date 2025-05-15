package redis.api

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk

case class SenMasters() extends RedisCommandMultiBulk[Seq[Map[String, String]]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SENTINEL MASTERS")

  def decodeReply(mb: MultiBulk): Seq[Map[String, String]] = MultiBulkConverter.toSeqMapString(mb)
}
