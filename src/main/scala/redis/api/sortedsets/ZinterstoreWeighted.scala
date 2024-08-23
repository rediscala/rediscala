package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.Aggregate
import redis.api.SUM

case class ZinterstoreWeighted[KD: ByteStringSerializer, K: ByteStringSerializer](destination: KD, keys: Map[K, Double], aggregate: Aggregate = SUM)
    extends RedisCommandIntegerLong {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ZINTERSTORE", ZstoreWeighted.buildArgs(destination, keys, aggregate))
}
