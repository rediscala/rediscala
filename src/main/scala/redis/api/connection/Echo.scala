package redis.api.connection

import org.apache.pekko.util.ByteString
import redis.*

case class Echo[V, R](value: V)(implicit convert: ByteStringSerializer[V], deserializerR: ByteStringDeserializer[R])
    extends RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("ECHO", Seq(convert.serialize(value)))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}
