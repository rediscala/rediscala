package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case class ConfigSet(parameter: String, value: String) extends RedisCommandStatusBoolean {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CONFIG", Seq(ByteString("SET"), ByteString(parameter), ByteString(value)))
}
