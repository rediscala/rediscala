package redis.api

import redis._
import akka.util.ByteString
import redis.protocol.MultiBulk

case class SenMasters() extends RedisCommandMultiBulk[Seq[Map[String, String]]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SENTINEL MASTERS")

  def decodeReply(mb: MultiBulk) = MultiBulkConverter.toSeqMapString(mb)
}

case class SenSlaves(master: String) extends RedisCommandMultiBulk[Seq[Map[String, String]]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL SLAVES $master")

  def decodeReply(mb: MultiBulk) = MultiBulkConverter.toSeqMapString(mb)
}

case class SenMasterInfo(master: String) extends RedisCommandMultiBulk[Map[String, String]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL master $master")

  def decodeReply(mb: MultiBulk) = MultiBulkConverter.toMapString(mb)
}

case class SenGetMasterAddr(master: String) extends RedisCommandMultiBulk[Option[Seq[String]]] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL get-master-addr-by-name $master")

  def decodeReply(mb: MultiBulk) = mb.responses.map(_.map(_.toString))
}

case class SenResetMaster(pattern: String) extends RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL RESET $pattern")
}

case class SenMasterFailover(master: String) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode(s"SENTINEL FAILOVER $master")
}
