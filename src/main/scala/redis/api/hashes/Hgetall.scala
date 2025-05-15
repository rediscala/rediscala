package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk
import redis.protocol.RedisReply
import scala.annotation.tailrec
import scala.collection.mutable

case class Hgetall[K, R](key: K)(using redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Map[String, R]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HGETALL", Seq(keyAsString))

  def decodeReply(mb: MultiBulk): Map[String, R] = mb.responses
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
