package redis.api.connection

import org.apache.pekko.util.ByteString
import redis.*

case object Quit extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("QUIT")
}
