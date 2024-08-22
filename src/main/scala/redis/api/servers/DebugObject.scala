package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case class DebugObject[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends RedisCommandStatusString {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("DEBUG", Seq(ByteString("OBJECT"), redisKey.serialize(key)))
}
