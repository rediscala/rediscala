package redis

import redis.protocol.RedisReply
import scala.collection.immutable.Queue
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

trait BufferedRequest {
  implicit val executionContext: ExecutionContext

  val operations: mutable.Builder[Operation[?, ?], Queue[Operation[?, ?]]] = Queue.newBuilder[Operation[?, ?]]

  def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    val promise = Promise[T]()
    operations += Operation(redisCommand, promise)
    promise.future
  }
}
