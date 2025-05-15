package redis.api.hashes

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk

case class Hmget[K, KK, R](key: K, fields: Seq[KK])(implicit
  redisKey: ByteStringSerializer[K],
  redisFields: ByteStringSerializer[KK],
  deserializerR: ByteStringDeserializer[R]
) extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[Option[R]]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("HMGET", keyAsString +: fields.map(redisFields.serialize))

  def decodeReply(mb: MultiBulk): Seq[Option[R]] = MultiBulkConverter.toSeqOptionByteString(mb)
}
