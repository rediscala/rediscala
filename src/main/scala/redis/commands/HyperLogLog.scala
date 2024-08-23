package redis.commands

import redis.ByteStringSerializer
import redis.Request
import redis.api.hyperloglog.*
import scala.concurrent.Future

trait HyperLogLog extends Request {
  def pfadd[V: ByteStringSerializer](key: String, values: V*): Future[Long] =
    send(Pfadd(key, values))

  def pfcount(keys: String*): Future[Long] =
    send(Pfcount(keys))

  def pfmerge(destKey: String, sourceKeys: String*): Future[Boolean] =
    send(Pfmerge(destKey, sourceKeys))
}
