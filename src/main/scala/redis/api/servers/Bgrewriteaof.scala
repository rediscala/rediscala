package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case object Bgrewriteaof extends RedisCommandStatusString {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("BGREWRITEAOF")
}
