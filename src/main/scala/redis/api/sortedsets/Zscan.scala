package redis.api.sortedsets

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.RedisReply

case class Zscan[K, C, R](key: K, cursor: C, count: Option[Int], matchGlob: Option[String])(implicit
  redisKey: ByteStringSerializer[K],
  redisCursor: ByteStringSerializer[C],
  deserializerR: ByteStringDeserializer[R],
  scoreDeserializer: ByteStringDeserializer[Double]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkCursor[Seq[(Double, R)]]
    with ByteStringDeserializerDefault {
  def isMasterOnly: Boolean = false
  val encodedRequest: ByteString = encode("ZSCAN", withOptionalParams(Seq(keyAsString, redisCursor.serialize(cursor))))

  val empty: Seq[(Double, R)] = Seq.empty

  def decodeResponses(responses: Seq[RedisReply]): Seq[(Double, R)] =
    responses
      .grouped(2)
      .map { xs =>
        val data = xs.head
        val score = scoreDeserializer.deserialize(xs(1).toByteString)
        score -> deserializerR.deserialize(data.toByteString)
      }
      .toSeq
}
