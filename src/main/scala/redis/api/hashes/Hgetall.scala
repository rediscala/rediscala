package redis.api.hashes

import redis.*
import redis.RediscalaCompat.util.ByteString
import scala.collection.mutable
import scala.annotation.tailrec
import redis.protocol.RedisReply
import redis.protocol.MultiBulk

case class Hgetall[K, R](key: K)(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Map[String, R]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HGETALL", Seq(keyAsString))

  def decodeReply(mb: MultiBulk) = mb.responses
    .map(r => {
      val builder = Map.newBuilder[String, R]
      builder.sizeHint(r.length / 2)
      seqToMap(r, builder)
      builder.result()
    })
    .get

  @tailrec
  private def seqToMap(seq: Vector[RedisReply], builder: mutable.Builder[(String, R), Map[String, R]]): Unit = {
    if (seq.nonEmpty) {
      val head = seq.head.toByteString
      val tail = seq.tail
      builder += (head.utf8String -> deserializerR.deserialize(tail.head.toByteString))
      seqToMap(tail.tail, builder)
    }
  }
}
