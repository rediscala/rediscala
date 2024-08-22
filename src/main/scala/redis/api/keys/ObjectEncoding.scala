package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.*

case class ObjectEncoding[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandBulk[Option[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("OBJECT", Seq(ByteString("ENCODING"), keyAsString))

  def decodeReply(bulk: Bulk) = bulk.toOptString
}
