package redis

import redis.protocol.*

trait RedisCommandBulkOptionByteString[R] extends RedisCommandBulk[Option[R]] {
  val deserializer: ByteStringDeserializer[R]

  def decodeReply(bulk: Bulk) = bulk.response.map(deserializer.deserialize)
}
