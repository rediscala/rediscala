package redis

import org.apache.pekko.actor.ActorSystem

case class RedisClientPool(redisServers: Seq[RedisServer], name: String = "RedisClientPool")(implicit
  _system: ActorSystem,
  redisDispatcher: RedisDispatcher = Redis.dispatcher
) extends RedisClientPoolLike(_system, redisDispatcher)
    with RoundRobinPoolRequest
    with RedisCommands {

  override val redisServerConnections: collection.Map[RedisServer, RedisConnection] = {
    redisServers.map { server =>
      makeRedisConnection(server, defaultActive = true)
    }.toMap
  }

  refreshConnections()

}
