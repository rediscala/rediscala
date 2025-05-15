package redis.api.scripting

import org.apache.pekko.util.ByteString
import redis.*

case class Eval[R, KK, KA](script: String, keys: Seq[KK] = Seq(), args: Seq[KA] = Seq())(implicit
  redisKeys: ByteStringSerializer[KK],
  redisArgs: ByteStringSerializer[KA],
  deserializerR: RedisReplyDeserializer[R]
) extends RedisCommandRedisReplyRedisReply[R]
    with EvaledScript {
  val encodedRequest: ByteString = encodeRequest(encode, "EVAL", script, keys, args, redisKeys, redisArgs)
  val deserializer: RedisReplyDeserializer[R] = deserializerR
}
