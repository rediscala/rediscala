package redis

import redis.protocol.*

trait RedisCommandBulkDouble extends RedisCommandBulk[Double] {
  def decodeReply(bulk: Bulk): Double = bulk.response.map(ByteStringDeserializer.RedisDouble.deserialize).get
}
