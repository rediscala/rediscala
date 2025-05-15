package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*

case class Hexists[K, KK](key: K, field: KK)(using redisKey: ByteStringSerializer[K], redisFields: ByteStringSerializer[KK])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerBoolean {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HEXISTS", Seq(keyAsString, redisFields.serialize(field)))
}
