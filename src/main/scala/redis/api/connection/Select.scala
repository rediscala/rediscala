package redis.api.connection

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Select(index: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SELECT", Seq(ByteString(index.toString)))
}
