package redis.api.servers

import org.apache.pekko.util.ByteString
import redis.*

case object ClientGetname extends RedisCommandBulkOptionByteString[String] {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CLIENT", Seq(ByteString("GETNAME")))
  val deserializer: ByteStringDeserializer[String] = ByteStringDeserializer.String
}
