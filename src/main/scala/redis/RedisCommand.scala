package redis

import redis.RediscalaCompat.util.ByteString
import redis.protocol.*

trait RedisCommand[RedisReplyT <: RedisReply, +T] {
  def isMasterOnly: Boolean
  val encodedRequest: ByteString

  def decodeReply(r: RedisReplyT): T

  val decodeRedisReply: PartialFunction[ByteString, DecodeResult[RedisReplyT]]

  def encode(command: String): ByteString = RedisProtocolRequest.inline(command)

  def encode(command: String, args: Seq[ByteString]): ByteString = RedisProtocolRequest.multiBulk(command, args)
}
