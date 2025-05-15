package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*

case class Move[K](key: K, db: Int)(using ByteStringSerializer[K]) extends SimpleClusterKey[K] with RedisCommandIntegerBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("MOVE", Seq(keyAsString, ByteString(db.toString)))
}
