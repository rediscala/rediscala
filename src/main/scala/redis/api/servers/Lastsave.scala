package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case object Lastsave extends RedisCommandIntegerLong {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("LASTSAVE")
}
