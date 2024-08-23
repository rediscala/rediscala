package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.Aggregate
import redis.api.SUM

case class ZunionstoreWeighted[KD: ByteStringSerializer, K: ByteStringSerializer](destination: KD, keys: Map[K, Double], aggregate: Aggregate = SUM)
    extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZUNIONSTORE", ZstoreWeighted.buildArgs(destination, keys, aggregate))
}
