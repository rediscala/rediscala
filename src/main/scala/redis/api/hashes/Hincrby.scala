package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*

case class Hincrby[K, KK](key: K, fields: KK, increment: Long)(using redisKey: ByteStringSerializer[K], redisFields: ByteStringSerializer[KK])
    extends SimpleClusterKey[K]
    with RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("HINCRBY", Seq(keyAsString, redisFields.serialize(fields), ByteString(increment.toString)))
}
