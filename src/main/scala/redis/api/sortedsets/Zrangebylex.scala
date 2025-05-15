package redis.api.sortedsets

import org.apache.pekko.util.ByteString
import redis.*

case class Zrangebylex[K, R](key: K, min: String, max: String, limit: Option[(Long, Long)] = None)(using
  keySeria: ByteStringSerializer[K],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("ZRANGEBYLEX", Zrangebylex.buildArgs(keyAsString, min, max, limit))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}

private[redis] object Zrangebylex {
  def buildArgs(key: ByteString, min: String, max: String, limit: Option[(Long, Long)]): Seq[ByteString] = {
    val builder = Seq.newBuilder[ByteString]
    builder ++= Seq(key, ByteString(min), ByteString(max))
    limit.foreach(l => {
      builder ++= Seq(ByteString("LIMIT"), ByteString(l._1.toString), ByteString(l._2.toString))
    })
    builder.result()
  }
}
