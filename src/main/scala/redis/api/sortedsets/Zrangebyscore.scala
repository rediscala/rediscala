package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.Limit

case class Zrangebyscore[K: ByteStringSerializer, R](key: K, min: Limit, max: Limit, limit: Option[(Long, Long)] = None)(using
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZRANGEBYSCORE", Zrangebyscore.buildArgs(key, min, max, withscores = false, limit))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}

private[redis] object Zrangebyscore {
  def buildArgs[K](key: K, min: Limit, max: Limit, withscores: Boolean, limit: Option[(Long, Long)])(using
    keySeria: ByteStringSerializer[K]
  ): Seq[ByteString] = {
    val builder = Seq.newBuilder[ByteString]
    builder ++= Seq(keySeria.serialize(key), min.toByteString, max.toByteString)
    if (withscores) {
      builder += ByteString("WITHSCORES")
    }
    limit.foreach(l => {
      builder ++= Seq(ByteString("LIMIT"), ByteString(l._1.toString), ByteString(l._2.toString))
    })
    builder.result()
  }
}
