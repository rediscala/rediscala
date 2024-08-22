package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString

case object ClientGetname extends RedisCommandBulkOptionByteString[String] {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CLIENT", Seq(ByteString("GETNAME")))
  val deserializer: ByteStringDeserializer[String] = ByteStringDeserializer.String
}
