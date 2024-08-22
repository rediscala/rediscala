package redis

import redis.protocol.*

trait RedisCommandBulkOptionDouble extends RedisCommandBulk[Option[Double]] {
  def decodeReply(bulk: Bulk) = bulk.response.map(ByteStringDeserializer.RedisDouble.deserialize)
}
