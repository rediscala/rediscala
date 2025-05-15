package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.LimitOffsetCount
import redis.api.Order

case class SortStore[K: ByteStringSerializer, KS: ByteStringSerializer](
  key: K,
  byPattern: Option[String],
  limit: Option[LimitOffsetCount],
  getPatterns: Seq[String],
  order: Option[Order],
  alpha: Boolean,
  store: KS
) extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SORT", Sort.buildArgs(key, byPattern, limit, getPatterns, order, alpha, Some(store)))
}
