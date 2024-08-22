package redis

import redis.protocol.RedisReply
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait Request {
  implicit val executionContext: ExecutionContext

  def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T]
}
