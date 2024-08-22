package redis.api.clusters

import redis.RediscalaCompat.util.ByteString
import redis.RedisCommand
import redis.protocol.DecodeResult
import redis.protocol.Bulk
import redis.protocol.RedisProtocolReply

case class ClusterInfo() extends RedisCommand[Bulk, Map[String, String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("CLUSTER INFO")
  def decodeReply(b: Bulk): Map[String, String] = {
    b.response.map(_.utf8String.split("\r\n").map(_.split(":")).map(s => (s(0), s(1))).toMap).getOrElse(Map.empty)
  }
  override val decodeRedisReply: PartialFunction[ByteString, DecodeResult[Bulk]] = { case s =>
    RedisProtocolReply.decodeReplyBulk(s)
  }
}
