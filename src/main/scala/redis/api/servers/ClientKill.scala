package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case class ClientKill(ip: String, port: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CLIENT", Seq(ByteString("KILL"), ByteString(ip + ":" + port)))
}
