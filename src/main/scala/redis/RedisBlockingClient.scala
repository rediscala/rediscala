package redis

import org.apache.pekko.actor.*
import redis.commands.*
import scala.concurrent.duration.FiniteDuration

case class RedisBlockingClient(
  var host: String = "localhost",
  var port: Int = 6379,
  override val username: Option[String] = None,
  override val password: Option[String] = None,
  override val db: Option[Int] = None,
  name: String = "RedisBlockingClient",
  connectTimeout: Option[FiniteDuration] = None
)(using _system: ActorSystem, redisDispatcher: RedisDispatcher = Redis.dispatcher)
    extends RedisClientActorLike(_system, redisDispatcher, connectTimeout)
    with BLists {}
