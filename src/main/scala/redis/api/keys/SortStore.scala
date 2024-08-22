package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.Order
import redis.api.LimitOffsetCount

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
