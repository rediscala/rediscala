package redis

import redis.protocol.*

trait RedisReplyDeserializerDefault {

  given String: RedisReplyDeserializer[String] =
    new RedisReplyDeserializer[String] {
      def deserialize: PartialFunction[RedisReply, String] = { case Bulk(Some(bs)) =>
        bs.utf8String
      }
    }

}
