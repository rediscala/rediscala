package redis.api.publish

import redis.ByteStringSerializer
import redis.RedisCommandIntegerLong
import redis.RediscalaCompat.util.ByteString

case class Publish[A](channel: String, value: A)(implicit convert: ByteStringSerializer[A]) extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PUBLISH", Seq(ByteString(channel), convert.serialize(value)))
}
