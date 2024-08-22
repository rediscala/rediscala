package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case class ClientKill(ip: String, port: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CLIENT", Seq(ByteString("KILL"), ByteString(ip + ":" + port)))
}
