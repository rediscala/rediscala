package redis

import redis.RediscalaCompat.util.ByteString
import redis.protocol.DecodeResult
import redis.protocol.RedisReply
import scala.concurrent.Promise

case class Operation[RedisReplyT <: RedisReply, T](redisCommand: RedisCommand[RedisReplyT, T], promise: Promise[T]) {
  def decodeRedisReplyThenComplete(bs: ByteString): DecodeResult[Unit] = {
    val r = redisCommand.decodeRedisReply.apply(bs)
    r.foreach { completeSuccess }
  }

  def completeSuccess(redisReply: RedisReplyT): Promise[T] = {
    val v = redisCommand.decodeReply(redisReply)
    promise.success(v)
  }

  def tryCompleteSuccess(redisReply: RedisReply): Boolean = {
    val v = redisCommand.decodeReply(redisReply.asInstanceOf[RedisReplyT])
    promise.trySuccess(v)
  }

  def completeSuccessValue(value: T): Promise[T] = promise.success(value)

  def completeFailed(t: Throwable): Promise[T] = promise.failure(t)
}
