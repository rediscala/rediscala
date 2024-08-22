package redis.api.connection

import redis.*
import redis.RediscalaCompat.util.ByteString

case object Quit extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("QUIT")
}
