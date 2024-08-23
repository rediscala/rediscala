package redis.api.blists

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.Bulk
import redis.protocol.RedisReply
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

case class Brpoplpush[KS, KD, R](source: KS, destination: KD, timeout: FiniteDuration = Duration.Zero)(implicit
  bsSource: ByteStringSerializer[KS],
  bsDest: ByteStringSerializer[KD],
  deserializerR: ByteStringDeserializer[R]
) extends RedisCommandRedisReply[Option[R]] {
  def isMasterOnly = true
  val encodedRequest: ByteString =
    encode("BRPOPLPUSH", Seq(bsSource.serialize(source), bsDest.serialize(destination), ByteString(timeout.toSeconds.toString)))

  def decodeReply(redisReply: RedisReply): Option[R] = redisReply match {
    case b: Bulk => b.asOptByteString.map(deserializerR.deserialize)
    case _ => None
  }
}
