package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case object Lastsave extends RedisCommandIntegerLong {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("LASTSAVE")
}
