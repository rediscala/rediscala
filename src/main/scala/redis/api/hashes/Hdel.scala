package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*

case class Hdel[K, KK](key: K, fields: Seq[KK])(using redisKey: ByteStringSerializer[K], redisFields: ByteStringSerializer[KK])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("HDEL", keyAsString +: fields.map(redisFields.serialize))
}
