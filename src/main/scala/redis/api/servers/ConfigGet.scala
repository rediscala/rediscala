package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk

case class ConfigGet(parameter: String) extends RedisCommandMultiBulk[Map[String, String]] {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CONFIG", Seq(ByteString("GET"), ByteString(parameter)))

  def decodeReply(r: MultiBulk): Map[String, String] = MultiBulkConverter.toMapString(r)
}
