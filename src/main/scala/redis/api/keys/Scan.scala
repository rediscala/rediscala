package redis.api.keys

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.*

case class Scan[C](cursor: C, count: Option[Int], matchGlob: Option[String])(implicit
  redisCursor: ByteStringSerializer[C],
  deserializer: ByteStringDeserializer[String]
) extends RedisCommandMultiBulkCursor[Seq[String]] {
  val encodedRequest: ByteString = encode("SCAN", withOptionalParams(Seq(redisCursor.serialize(cursor))))

  def isMasterOnly = false

  def decodeResponses(responses: Seq[RedisReply]): Seq[String] =
    responses.map(response => deserializer.deserialize(response.toByteString))

  val empty: Seq[String] = Seq.empty
}
