package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Sdiffstore[KD, K, KK](destination: KD, key: K, keys: Seq[KK])(implicit
  redisDest: ByteStringSerializer[KD],
  redisKey: ByteStringSerializer[K],
  redisKeys: ByteStringSerializer[KK]
) extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SDIFFSTORE", redisDest.serialize(destination) +: redisKey.serialize(key) +: keys.map(redisKeys.serialize))
}
