package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Slaveof(ip: String, port: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("SLAVEOF", Seq(ByteString(ip), ByteString(port.toString)))
}
