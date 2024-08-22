package redis

import redis.RediscalaCompat.util.ByteString
import redis.protocol.*

trait RedisCommandMultiBulkCursor[R] extends RedisCommandMultiBulk[Cursor[R]] {
  def decodeReply(mb: MultiBulk) = {
    mb.responses.map { responses =>
      val cursor = ParseNumber.parseInt(responses.head.toByteString)
      val remainder = responses(1).asInstanceOf[MultiBulk]

      Cursor(cursor, remainder.responses.map(decodeResponses).getOrElse(empty))
    }.getOrElse(Cursor(0, empty))
  }

  def decodeResponses(responses: Seq[RedisReply]): R

  val empty: R
  val count: Option[Int]
  val matchGlob: Option[String]

  def withOptionalParams(params: Seq[ByteString]): Seq[ByteString] = {
    val withCount = count.fold(params)(c => params ++ Seq(ByteString("COUNT"), ByteString(c.toString)))
    matchGlob.fold(withCount)(m => withCount ++ Seq(ByteString("MATCH"), ByteString(m)))
  }
}
