package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*

case class Sinterstore[KD, K, KK](destination: KD, key: K, keys: Seq[KK])(implicit
  redisDest: ByteStringSerializer[KD],
  redisKey: ByteStringSerializer[K],
  redisKeys: ByteStringSerializer[KK]
) extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SINTERSTORE", redisDest.serialize(destination) +: redisKey.serialize(key) +: keys.map(redisKeys.serialize))
}
