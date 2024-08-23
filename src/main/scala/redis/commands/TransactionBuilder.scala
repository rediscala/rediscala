package redis.commands

import redis.*
import redis.RediscalaCompat.actor.*
import redis.protocol.MultiBulk
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

case class TransactionBuilder(redisConnection: ActorRef)(implicit val executionContext: ExecutionContext) extends BufferedRequest with RedisCommands {

  // val operations = Queue.newBuilder[Operation[_, _]]
  val watcher = Set.newBuilder[String]

  def unwatch(): Unit = {
    watcher.clear()
  }

  def watch(keys: String*): Unit = {
    watcher ++= keys
  }

  def discard(): Unit = {
    operations
      .result()
      .map(operation => {
        operation.completeFailed(TransactionDiscardedException)
      })
    operations.clear()
    unwatch()
  }

  // todo maybe return a Future for the general state of the transaction ? (Success or Failure)
  def exec(): Future[MultiBulk] = {
    val t = redis.commands.Transaction(watcher.result(), operations.result(), redisConnection)
    val p = Promise[MultiBulk]()
    t.process(p)
    p.future
  }
}
