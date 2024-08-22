package redis.api.strings

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.MultiBulk

case class Mget[K, R](keys: Seq[K])(implicit redisKey: ByteStringSerializer[K], deserializerR: ByteStringDeserializer[R])
    extends MultiClusterKey[K]
    with RedisCommandMultiBulk[Seq[Option[R]]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("MGET", keys.map(redisKey.serialize))

  def decodeReply(mb: MultiBulk): Seq[Option[R]] = mb.responses
    .map(res => {
      res.map(_.asOptByteString.map(deserializerR.deserialize))
    })
    .get
}
