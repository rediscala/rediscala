package redis

import redis.RediscalaCompat.util.ByteString
import redis.protocol.*

trait RedisCommandMultiBulk[T] extends RedisCommand[MultiBulk, T] {
  val decodeRedisReply: PartialFunction[ByteString, DecodeResult[MultiBulk]] = RedisProtocolReply.decodeReplyMultiBulk
}
