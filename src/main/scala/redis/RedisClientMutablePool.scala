package redis

import org.apache.pekko.actor.ActorSystem

case class RedisClientMutablePool(redisServers: Seq[RedisServer], name: String = "RedisClientPool")(implicit
  system: ActorSystem,
  redisDispatcher: RedisDispatcher = Redis.dispatcher
) extends RedisClientPoolLike(system, redisDispatcher)
    with RoundRobinPoolRequest
    with RedisCommands {

  override val redisServerConnections: collection.mutable.Map[RedisServer, RedisConnection] = {
    val m = redisServers map { server => makeRedisConnection(server) }
    collection.mutable.Map(m*)
  }

  def addServer(server: RedisServer): Unit = {
    if (!redisServerConnections.contains(server)) {
      redisServerConnections.synchronized {
        if (!redisServerConnections.contains(server)) {
          redisServerConnections += makeRedisConnection(server)
        }
      }
    }
  }

  def removeServer(askServer: RedisServer): Unit = {
    if (redisServerConnections.contains(askServer)) {
      redisServerConnections.synchronized {
        redisServerConnections.get(askServer).foreach { redisServerConnection =>
          system stop redisServerConnection.actor
        }
        redisServerConnections.remove(askServer)
        refreshConnections()
      }
    }
  }

}
