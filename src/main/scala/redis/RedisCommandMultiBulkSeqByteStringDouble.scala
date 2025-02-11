package redis

import redis.protocol.*

trait RedisCommandMultiBulkSeqByteStringDouble[R] extends RedisCommandMultiBulk[Seq[(R, Double)]] {
  val deserializer: ByteStringDeserializer[R]

  def decodeReply(mb: MultiBulk): Seq[(R, Double)] = MultiBulkConverter.toSeqTuple2ByteStringDouble(mb)(using deserializer)
}
