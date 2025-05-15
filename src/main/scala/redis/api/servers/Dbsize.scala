package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case object Dbsize extends RedisCommandIntegerLong {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("DBSIZE")
}
