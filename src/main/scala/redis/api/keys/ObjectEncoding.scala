package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.*

case class ObjectEncoding[K](key: K)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandBulk[Option[String]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("OBJECT", Seq(ByteString("ENCODING"), keyAsString))

  def decodeReply(bulk: Bulk) = bulk.toOptString
}
