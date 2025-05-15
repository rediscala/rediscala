package redis

import org.apache.pekko.util.ByteString
import redis.protocol.*

trait RedisCommandRedisReply[T] extends RedisCommand[RedisReply, T] {
  val decodeRedisReply: PartialFunction[ByteString, DecodeResult[RedisReply]] = RedisProtocolReply.decodeReplyPF
}
