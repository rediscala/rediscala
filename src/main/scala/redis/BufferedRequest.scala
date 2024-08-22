package redis

import redis.protocol.RedisReply
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise
import scala.concurrent.Future
import scala.collection.immutable.Queue

trait BufferedRequest {
  implicit val executionContext: ExecutionContext

  val operations = Queue.newBuilder[Operation[?, ?]]

  def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    val promise = Promise[T]()
    operations += Operation(redisCommand, promise)
    promise.future
  }
}
