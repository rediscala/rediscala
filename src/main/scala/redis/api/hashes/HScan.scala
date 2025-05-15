package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.RedisReply

case class HScan[K, C, R](key: K, cursor: C, count: Option[Int], matchGlob: Option[String])(using
  redisKey: ByteStringSerializer[K],
  deserializer: ByteStringDeserializer[R],
  cursorConverter: ByteStringSerializer[C]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulkCursor[Map[String, R]] {
  def isMasterOnly: Boolean = false

  val encodedRequest: ByteString = encode("HSCAN", withOptionalParams(Seq(keyAsString, cursorConverter.serialize(cursor))))

  def decodeResponses(responses: Seq[RedisReply]): Map[String, R] =
    responses
      .grouped(2)
      .map { xs =>
        val k = xs.head
        val v = xs(1)

        k.toByteString.utf8String -> deserializer.deserialize(v.toByteString)
      }
      .toMap

  val empty: Map[String, R] = Map.empty
}
