package redis

import org.apache.pekko.util.ByteString
import redis.protocol.*

trait RedisCommandStatus[T] extends RedisCommand[Status, T] {
  val decodeRedisReply: PartialFunction[ByteString, DecodeResult[Status]] = RedisProtocolReply.decodeReplyStatus
}
