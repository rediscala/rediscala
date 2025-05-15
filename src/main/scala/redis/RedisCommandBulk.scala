package redis

import org.apache.pekko.util.ByteString
import redis.protocol.*

trait RedisCommandBulk[T] extends RedisCommand[Bulk, T] {
  val decodeRedisReply: PartialFunction[ByteString, DecodeResult[Bulk]] = RedisProtocolReply.decodeReplyBulk
}
