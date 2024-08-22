package redis.api.lists

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Rpoplpush[KS, KD, R](source: KS, destination: KD)(implicit
  sourceSer: ByteStringSerializer[KS],
  destSer: ByteStringSerializer[KD],
  deserializerR: ByteStringDeserializer[R]
) extends RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("RPOPLPUSH", Seq(sourceSer.serialize(source), destSer.serialize(destination)))
  val deserializer: ByteStringDeserializer[R] = deserializerR

}
