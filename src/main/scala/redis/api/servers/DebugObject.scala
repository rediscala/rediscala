package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case class DebugObject[K](key: K)(using redisKey: ByteStringSerializer[K]) extends RedisCommandStatusString {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("DEBUG", Seq(ByteString("OBJECT"), redisKey.serialize(key)))
}
