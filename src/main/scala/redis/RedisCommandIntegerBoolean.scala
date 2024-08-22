package redis

import redis.protocol.*

trait RedisCommandIntegerBoolean extends RedisCommandInteger[Boolean] {
  def decodeReply(i: Integer): Boolean = i.toBoolean
}
