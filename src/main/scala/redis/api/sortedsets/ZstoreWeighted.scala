package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.Aggregate
import redis.api.SUM

private[redis] object ZstoreWeighted {
  def buildArgs[KD, K](destination: KD, keys: Map[K, Double], aggregate: Aggregate = SUM)(using
    keyDestSeria: ByteStringSerializer[KD],
    keySeria: ByteStringSerializer[K]
  ): Seq[ByteString] = {
    (keyDestSeria.serialize(destination) +: ByteString(keys.size.toString) +: keys.keys.map(keySeria.serialize).toSeq) ++ (ByteString(
      "WEIGHTS"
    ) +: keys.values.map(v => ByteString(v.toString)).toSeq) ++ Seq(ByteString("AGGREGATE"), ByteString(aggregate.toString))
  }
}
