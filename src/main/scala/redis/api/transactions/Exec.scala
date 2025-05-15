package redis.api.transactions

import org.apache.pekko.util.ByteString
import redis.RedisCommandMultiBulk
import redis.protocol.MultiBulk

case object Exec extends RedisCommandMultiBulk[MultiBulk] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("EXEC")

  def decodeReply(r: MultiBulk): MultiBulk = r
}
