package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Move[K](key: K, db: Int)(implicit redisKey: ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("MOVE", Seq(keyAsString, ByteString(db.toString)))
}
