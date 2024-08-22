package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Hdel[K, KK](key: K, fields: Seq[KK])(implicit redisKey: ByteStringSerializer[K], redisFields: ByteStringSerializer[KK])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("HDEL", keyAsString +: fields.map(redisFields.serialize))
}
