package redis.api.strings

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.BitOperator

case class Bitop[K, KK](operation: BitOperator, destkey: K, keys: Seq[KK])(using
  redisKey: ByteStringSerializer[K],
  redisKeys: ByteStringSerializer[KK]
) extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("BITOP", Seq(ByteString(operation.toString), redisKey.serialize(destkey)) ++ keys.map(redisKeys.serialize))
}
