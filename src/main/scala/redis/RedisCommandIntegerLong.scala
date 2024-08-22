package redis

import redis.protocol.*

trait RedisCommandIntegerLong extends RedisCommandInteger[Long] {
  def decodeReply(i: Integer) = i.toLong
}
