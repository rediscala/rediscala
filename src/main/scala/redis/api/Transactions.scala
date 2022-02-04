package redis.api.transactions

import redis.RedisCommandMultiBulk
import redis.RedisCommandStatusBoolean
import akka.util.ByteString
import redis.protocol.MultiBulk

case object Multi extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("MULTI")
}

case object Exec extends RedisCommandMultiBulk[MultiBulk] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("EXEC")

  def decodeReply(r: MultiBulk): MultiBulk = r
}

case class Watch(keys: Set[String]) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("WATCH", keys.map(ByteString.apply).toSeq)
}
