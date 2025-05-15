package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case class ClientSetname(connectionName: String) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CLIENT", Seq(ByteString("SETNAME"), ByteString(connectionName)))
}
