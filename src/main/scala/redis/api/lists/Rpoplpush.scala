package redis.api.lists

import org.apache.pekko.util.ByteString
import redis.*

case class Rpoplpush[KS, KD, R](source: KS, destination: KD)(using
  sourceSer: ByteStringSerializer[KS],
  destSer: ByteStringSerializer[KD],
  deserializerR: ByteStringDeserializer[R]
) extends RedisCommandBulkOptionByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("RPOPLPUSH", Seq(sourceSer.serialize(source), destSer.serialize(destination)))
  val deserializer: ByteStringDeserializer[R] = deserializerR

}
