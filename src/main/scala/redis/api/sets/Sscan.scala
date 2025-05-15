package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.RedisReply

case class Sscan[K, C, R](key: K, cursor: C, count: Option[Int], matchGlob: Option[String])(implicit
  redisKey: ByteStringSerializer[K],
  redisCursor: ByteStringSerializer[C],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkCursor[Seq[R]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SSCAN", withOptionalParams(Seq(keyAsString, redisCursor.serialize(cursor))))

  val empty: Seq[R] = Seq.empty

  def decodeResponses(responses: Seq[RedisReply]): Seq[R] =
    responses.map(response => deserializerR.deserialize(response.toByteString))
}
