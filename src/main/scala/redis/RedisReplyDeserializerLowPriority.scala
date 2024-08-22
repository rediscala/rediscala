package redis

import redis.protocol.*

trait RedisReplyDeserializerLowPriority extends RedisReplyDeserializerDefault {

  implicit object RedisReply extends RedisReplyDeserializer[RedisReply] {
    def deserialize: PartialFunction[RedisReply, RedisReply] = { case reply =>
      reply
    }
  }

}
