package redis

import redis.protocol.*

trait RedisReplyDeserializerDefault {

  implicit object String extends RedisReplyDeserializer[String] {
    def deserialize: PartialFunction[RedisReply, String] = { case Bulk(Some(bs)) =>
      bs.utf8String
    }
  }

}
