package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case class Slaveof(ip: String, port: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("SLAVEOF", Seq(ByteString(ip), ByteString(port.toString)))
}
