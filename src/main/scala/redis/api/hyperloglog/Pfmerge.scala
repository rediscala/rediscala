package redis.api.hyperloglog

import redis.RediscalaCompat.util.ByteString
import redis.RedisCommandStatusBoolean
import redis.ByteStringSerializer

case class Pfmerge[K](destKey: K, sourceKeys: Seq[K])(implicit redisKey: ByteStringSerializer[K]) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("PFMERGE", redisKey.serialize(destKey) +: sourceKeys.map(redisKey.serialize))
}
