package redis

import redis.protocol.*

trait RedisCommandRedisReplyOptionLong extends RedisCommandRedisReply[Option[Long]] {
  def decodeReply(redisReply: RedisReply): Option[Long] = redisReply match {
    case i: Integer => Some(i.toLong)
    case _ => None
  }
}
