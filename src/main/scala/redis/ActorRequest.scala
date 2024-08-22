package redis

import redis.protocol.RedisReply
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise
import scala.concurrent.Future
import redis.RediscalaCompat.actor.ActorRef

trait ActorRequest {
  implicit val executionContext: ExecutionContext

  def redisConnection: ActorRef

  def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    val promise = Promise[T]()
    redisConnection ! Operation(redisCommand, promise)
    promise.future
  }
}
