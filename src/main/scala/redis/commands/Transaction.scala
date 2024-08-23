package redis.commands

import redis.*
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext
import redis.RediscalaCompat.actor.*
import scala.collection.immutable.Queue
import redis.actors.ReplyErrorException
import redis.protocol.*
import redis.protocol.MultiBulk
import scala.util.Failure
import scala.util.Success
import redis.api.transactions.Watch
import redis.api.transactions.Exec
import redis.api.transactions.Multi
import redis.RediscalaCompat.util.ByteString

case class Transaction(watcher: Set[String], operations: Queue[Operation[?, ?]], redisConnection: ActorRef)(implicit
  val executionContext: ExecutionContext
) {

  def process(promise: Promise[MultiBulk]): Unit = {
    val multiOp = Operation(Multi, Promise[Boolean]())
    val execOp = Operation(Exec, execPromise(promise))

    val commands = Seq.newBuilder[Operation[?, ?]]

    val watchOp = watchOperation(watcher)
    watchOp.map(commands.+=(_))
    commands += multiOp
    commands ++= operations.map(op => operationToQueuedOperation(op))
    commands += execOp

    redisConnection ! redis.Transaction(commands.result())
  }

  def operationToQueuedOperation(op: Operation[?, ?]) = {
    val cmd = new RedisCommandStatusString {
      def isMasterOnly = true
      val encodedRequest: ByteString = op.redisCommand.encodedRequest
    }
    Operation(cmd, Promise[String]())
  }

  def ignoredPromise() = Promise[Any]()

  def execPromise(promise: Promise[MultiBulk]): Promise[MultiBulk] = {
    val p = Promise[MultiBulk]()
    p.future.onComplete {
      case Success(m) =>
        promise.success(m)
        dispatchExecReply(m)
      case Failure(f) =>
        promise.failure(f)
        operations.foreach(_.completeFailed(f))
    }
    p
  }

  def dispatchExecReply(multiBulk: MultiBulk) = {
    multiBulk.responses
      .map(replies => {
        replies
          .lazyZip(operations)
          .map((reply, operation) => {
            reply match {
              case e: Error => operation.completeFailed(ReplyErrorException(e.toString()))
              case _ => operation.tryCompleteSuccess(reply)
            }
          })
      })
      .getOrElse {
        operations.foreach(_.completeFailed(TransactionWatchException()))
      }
  }

  def watchOperation(keys: Set[String]): Option[Operation[?, Boolean]] = {
    if (keys.nonEmpty) {
      Some(Operation(Watch(keys), Promise[Boolean]()))
    } else {
      None
    }
  }
}
