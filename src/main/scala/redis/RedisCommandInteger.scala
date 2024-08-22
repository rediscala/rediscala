package redis

import redis.RediscalaCompat.util.ByteString
import redis.protocol.*

trait RedisCommandInteger[T] extends RedisCommand[Integer, T] {
  val decodeRedisReply: PartialFunction[ByteString, DecodeResult[Integer]] = RedisProtocolReply.decodeReplyInteger
}
