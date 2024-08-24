package redis

import redis.protocol.*

trait RedisCommandBulkOptionByteString[R] extends RedisCommandBulk[Option[R]] {
  val deserializer: ByteStringDeserializer[R]

  def decodeReply(bulk: Bulk): Option[R] = bulk.response.map(deserializer.deserialize)
}
