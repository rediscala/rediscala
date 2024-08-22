package redis.api.clusters

import redis.RediscalaCompat.util.ByteString
import redis.RedisCommand
import redis.protocol.DecodeResult
import redis.protocol.Bulk
import redis.protocol.RedisProtocolReply

case class ClusterNodes() extends RedisCommand[Bulk, Array[ClusterNodeInfo]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("CLUSTER NODES")
  def decodeReply(b: Bulk): Array[ClusterNodeInfo] = {
    b.response
      .map(
        _.utf8String
          .split("\n")
          .map(_.split(" "))
          .map(s => ClusterNodeInfo(s(0), s(1), s(2), s(3), s(4).toLong, s(5).toLong, s(6).toLong, s(7), s.drop(8)))
      )
      .getOrElse(Array.empty)
  }
  override val decodeRedisReply: PartialFunction[ByteString, DecodeResult[Bulk]] = { case s =>
    RedisProtocolReply.decodeReplyBulk(s)
  }
}
