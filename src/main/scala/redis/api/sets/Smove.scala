package redis.api.sets

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Smove[KS, KD, V](source: KS, destination: KD, member: V)(implicit
  redisSource: ByteStringSerializer[KS],
  redisDest: ByteStringSerializer[KD],
  convert: ByteStringSerializer[V]
) extends RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SMOVE", Seq(redisSource.serialize(source), redisDest.serialize(destination), convert.serialize(member)))
}
