package redis.actors

import org.apache.pekko.util.ByteString
import redis.protocol.DecodeResult
import redis.protocol.RedisProtocolReply
import redis.protocol.RedisReply
import scala.annotation.tailrec

trait DecodeReplies {
  private var partiallyDecoded: DecodeResult[Unit] = DecodeResult.unit

  def decodeReplies(dataByteString: ByteString): Unit = {
    partiallyDecoded = if (partiallyDecoded.isFullyDecoded) {
      decodeRepliesRecur(dataByteString)
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
    val r = RedisProtocolReply.decodeReply(bs).map(onDecodedReply)
    if (r.isFullyDecoded) {
      decodeRepliesRecur(r.rest)
    } else {
      r
    }
  }

  def onDecodedReply(reply: RedisReply): Unit
}
