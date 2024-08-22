package redis.api.scripting

import redis.*
import redis.RediscalaCompat.util.ByteString

case class Evalsha[R, KK, KA](sha1: String, keys: Seq[KK] = Seq(), args: Seq[KA] = Seq())(implicit
  redisKeys: ByteStringSerializer[KK],
  redisArgs: ByteStringSerializer[KA],
  deserializerR: RedisReplyDeserializer[R]
) extends RedisCommandRedisReplyRedisReply[R]
    with EvaledScript {
  val encodedRequest: ByteString = encodeRequest(encode, "EVALSHA", sha1, keys, args, redisKeys, redisArgs)
  val deserializer: RedisReplyDeserializer[R] = deserializerR
}
