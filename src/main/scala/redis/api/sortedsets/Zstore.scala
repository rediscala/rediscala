package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.Aggregate
import redis.api.SUM

private[redis] object Zstore {
  def buildArgs[KD, K, KK](destination: KD, key: K, keys: Seq[KK], aggregate: Aggregate = SUM)(implicit
    keyDestSeria: ByteStringSerializer[KD],
    keySeria: ByteStringSerializer[K],
    keysSeria: ByteStringSerializer[KK]
  ): Seq[ByteString] = {
    (keyDestSeria.serialize(destination)
      +: ByteString((1 + keys.size).toString)
      +: keySeria.serialize(key)
      +: keys.map(keysSeria.serialize)) ++ Seq(ByteString("AGGREGATE"), ByteString(aggregate.toString))
  }
}
