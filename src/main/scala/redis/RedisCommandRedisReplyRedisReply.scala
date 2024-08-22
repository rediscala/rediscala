package redis

import redis.protocol.*

trait RedisCommandRedisReplyRedisReply[R] extends RedisCommandRedisReply[R] {
  val deserializer: RedisReplyDeserializer[R]

  def decodeReply(redisReply: RedisReply): R = {
    if (deserializer.deserialize.isDefinedAt(redisReply))
      deserializer.deserialize.apply(redisReply)
    else
      throw new RuntimeException("Could not deserialize") // todo make own type
  }
}
