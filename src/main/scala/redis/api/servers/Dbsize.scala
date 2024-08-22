package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case object Dbsize extends RedisCommandIntegerLong {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("DBSIZE")
}
