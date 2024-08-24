package redis

import redis.protocol.*

trait RedisCommandMultiBulkSeqByteString[R] extends RedisCommandMultiBulk[Seq[R]] {
  val deserializer: ByteStringDeserializer[R]

  def decodeReply(mb: MultiBulk): Seq[R] = MultiBulkConverter.toSeqByteString(mb)(deserializer)
}
