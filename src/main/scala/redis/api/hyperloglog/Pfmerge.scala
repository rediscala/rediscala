package redis.api.hyperloglog

import org.apache.pekko.util.ByteString
import redis.ByteStringSerializer
import redis.RedisCommandStatusBoolean

case class Pfmerge[K](destKey: K, sourceKeys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PFMERGE", redisKey.serialize(destKey) +: sourceKeys.map(redisKey.serialize))
}
