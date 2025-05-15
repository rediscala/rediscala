package redis.api.publish

import org.apache.pekko.util.ByteString
import redis.ByteStringSerializer
import redis.RedisCommandIntegerLong

case class Publish[A](channel: String, value: A)(implicit convert: ByteStringSerializer[A]) extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PUBLISH", Seq(ByteString(channel), convert.serialize(value)))
}
