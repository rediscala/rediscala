package redis

import redis.protocol.*

trait RedisReplyDeserializerLowPriority extends RedisReplyDeserializerDefault {

  given RedisReply: RedisReplyDeserializer[RedisReply] =
    new RedisReplyDeserializer[RedisReply] {
      def deserialize: PartialFunction[RedisReply, RedisReply] = { case reply =>
        reply
      }
    }

}
