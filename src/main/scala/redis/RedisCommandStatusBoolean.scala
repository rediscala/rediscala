package redis

import redis.protocol.*

trait RedisCommandStatusBoolean extends RedisCommandStatus[Boolean] {
  def decodeReply(s: Status): Boolean = s.toBoolean
}
