package redis.api

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.MultiBulk

case class SenGetMasterAddr(master: String) extends RedisCommandMultiBulk[Option[Seq[String]]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL get-master-addr-by-name $master")

  def decodeReply(mb: MultiBulk): Option[Seq[String]] = mb.responses.map(_.map(_.toString))
}
