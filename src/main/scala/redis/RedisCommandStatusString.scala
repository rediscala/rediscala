package redis

import redis.protocol.*

trait RedisCommandStatusString extends RedisCommandStatus[String] {
  def decodeReply(s: Status) = s.toString
}
