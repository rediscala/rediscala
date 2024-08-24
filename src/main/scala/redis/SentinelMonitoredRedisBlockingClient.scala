package redis

import redis.RediscalaCompat.actor.*
import redis.commands.*

case class SentinelMonitoredRedisBlockingClient(
  sentinels: Seq[(String, Int)] = Seq(("localhost", 26379)),
  master: String,
  username: Option[String] = None,
  password: Option[String] = None,
  db: Option[Int] = None,
  name: String = "SMRedisBlockingClient"
)(implicit system: ActorSystem, redisDispatcher: RedisDispatcher = Redis.dispatcher)
    extends SentinelMonitoredRedisClientLike(system, redisDispatcher)
    with BLists {
  val redisClient: RedisBlockingClient = withMasterAddr((ip, port) => {
    RedisBlockingClient(ip, port, username, password, db, name)
  })
  override val onNewSlave: (String, Int) => Unit = (ip: String, port: Int) => {}
  override val onSlaveDown: (String, Int) => Unit = (ip: String, port: Int) => {}
}
