package redis

import redis.protocol.*
import scala.annotation.implicitNotFound

object RedisReplyDeserializer extends RedisReplyDeserializerLowPriority

@implicitNotFound(
  msg = "No RedisReplyDeserializer deserializer found for type ${T}. Try to implement an `given RedisReplyDeserializer[${T}]` for this type."
)
trait RedisReplyDeserializer[T] {
  def deserialize: PartialFunction[RedisReply, T]
}
