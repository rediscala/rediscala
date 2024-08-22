package redis

import redis.RediscalaCompat.actor.ActorSystem

abstract class SentinelMonitoredRedisClientLike(system: ActorSystem, redisDispatcher: RedisDispatcher)
    extends SentinelMonitored(system, redisDispatcher)
    with ActorRequest {
  val redisClient: RedisClientActorLike
  val onMasterChange = (ip: String, port: Int) => {
    log.info(s"onMasterChange: $ip:$port")
    redisClient.reconnect(ip, port)
  }

  def redisConnection = redisClient.redisConnection

  /**
    * Disconnect from the server (stop the actors)
    */
  def stop() = {
    redisClient.stop()
    sentinelClients.values.foreach(_.stop())
  }

}
