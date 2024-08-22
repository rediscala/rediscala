package redis

import redis.RediscalaCompat.util.ByteString
import redis.protocol.*

trait RedisCommandRedisReply[T] extends RedisCommand[RedisReply, T] {
  val decodeRedisReply: PartialFunction[ByteString, DecodeResult[RedisReply]] = RedisProtocolReply.decodeReplyPF
}
