package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case class ClientSetname(connectionName: String) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CLIENT", Seq(ByteString("SETNAME"), ByteString(connectionName)))
}
