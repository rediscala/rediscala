package redis

import redis.protocol.*

trait RedisCommandMultiBulkSeqByteStringDouble[R] extends RedisCommandMultiBulk[Seq[(R, Double)]] {
  val deserializer: ByteStringDeserializer[R]

  def decodeReply(mb: MultiBulk) = MultiBulkConverter.toSeqTuple2ByteStringDouble(mb)(deserializer)
}
