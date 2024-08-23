package redis

import java.util.concurrent.atomic.AtomicInteger
import redis.RediscalaCompat.actor.ActorRef
import redis.protocol.RedisReply
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

trait RoundRobinPoolRequest {
  implicit val executionContext: ExecutionContext

  def redisConnectionPool: Seq[ActorRef]

  val next = new AtomicInteger(0)

  def getNextConnection: Option[ActorRef] = {
    val size = redisConnectionPool.size
    if (size == 0) {
      None
    } else {
      val index = next.getAndIncrement % size
      Some(redisConnectionPool(if (index < 0) size + index - 1 else index))
    }
  }

  protected def send[T](redisConnection: ActorRef, redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    val promise = Promise[T]()
    redisConnection ! Operation(redisCommand, promise)
    promise.future
  }

  def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    getNextConnection.fold(
      Future.failed[T](new RuntimeException("redis pool is empty"))
    ) { redisConnection =>
      send(redisConnection, redisCommand)
    }
  }

}
