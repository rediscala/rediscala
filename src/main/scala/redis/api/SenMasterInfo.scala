package redis.api

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.MultiBulk

case class SenMasterInfo(master: String) extends RedisCommandMultiBulk[Map[String, String]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL master $master")

  def decodeReply(mb: MultiBulk): Map[String, String] = MultiBulkConverter.toMapString(mb)
}
