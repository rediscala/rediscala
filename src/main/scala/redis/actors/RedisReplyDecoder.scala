package redis.actors

import org.apache.pekko.actor.Actor
import org.apache.pekko.event.Logging
import org.apache.pekko.util.ByteString
import redis.Operation
import redis.protocol.DecodeResult
import redis.protocol.FullyDecoded
import redis.protocol.RedisProtocolReply
import scala.annotation.tailrec
import scala.collection.mutable

class RedisReplyDecoder() extends Actor {

  val queuePromises = mutable.Queue[Operation[?, ?]]()

  val log = Logging(context.system, this)

  override def postStop(): Unit = {
    queuePromises.foreach(op => {
      op.completeFailed(InvalidRedisReply)
    })
  }

  def receive = {
    case promises: QueuePromises =>
      queuePromises ++= promises.queue
    case byteStringInput: ByteString => decodeReplies(byteStringInput)
  }

  private[redis] var partiallyDecoded: DecodeResult[Unit] = DecodeResult.unit

  def decodeReplies(dataByteString: ByteString): Unit = {
    partiallyDecoded = if (partiallyDecoded.isFullyDecoded) {
      decodeRepliesRecur(partiallyDecoded.rest ++ dataByteString)
    } else {
      val r = partiallyDecoded.run(dataByteString)
      if (r.isFullyDecoded) {
        decodeRepliesRecur(r.rest)
      } else {
        r
      }
    }
  }

  @tailrec
  private def decodeRepliesRecur(bs: ByteString): DecodeResult[Unit] = {
    if (queuePromises.nonEmpty && bs.nonEmpty) {
      val op = queuePromises.dequeue()
      val result = decodeRedisReply(op, bs)

      if (result.isFullyDecoded) {
        decodeRepliesRecur(result.rest)
      } else {
        result
      }
    } else {
      FullyDecoded((), bs)
    }
  }

  def decodeRedisReply(operation: Operation[?, ?], bs: ByteString): DecodeResult[Unit] = {
    if (operation.redisCommand.decodeRedisReply.isDefinedAt(bs)) {
      operation.decodeRedisReplyThenComplete(bs)
    } else if (RedisProtocolReply.decodeReplyError.isDefinedAt(bs)) {
      RedisProtocolReply.decodeReplyError.apply(bs).foreach { error =>
        operation.completeFailed(ReplyErrorException(error.toString))
      }
    } else {
      operation.completeFailed(InvalidRedisReply)
      throw new Exception(s"Redis Protocol error: Got ${bs.head} as initial reply byte for Operation: $operation")
    }
  }
}
