package redis.api.blists

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk
import scala.concurrent.duration.FiniteDuration

private[redis] abstract class BXpop[KK, R](command: String)(using redisKeys: ByteStringSerializer[KK], deserializerR: ByteStringDeserializer[R])
    extends RedisCommandMultiBulk[Option[(String, R)]] {
  def isMasterOnly = true
  val keys: Seq[KK]
  val timeout: FiniteDuration

  val encodedRequest: ByteString = encode(command, keys.map(redisKeys.serialize) ++ Seq(ByteString(timeout.toSeconds.toString)))

  def decodeReply(mb: MultiBulk): Option[(String, R)] = MultiBulkConverter.toOptionStringByteString(mb)
}
