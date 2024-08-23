package redis

import redis.protocol.RedisReply
import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

trait BufferedRequest {
  implicit val executionContext: ExecutionContext

  val operations = Queue.newBuilder[Operation[?, ?]]

  def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    val promise = Promise[T]()
    operations += Operation(redisCommand, promise)
    promise.future
  }
}
