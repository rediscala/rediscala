package redis

import redis.protocol.*

trait RedisCommandBulkOptionDouble extends RedisCommandBulk[Option[Double]] {
  def decodeReply(bulk: Bulk): Option[Double] = bulk.response.map(ByteStringDeserializer.RedisDouble.deserialize)
}
