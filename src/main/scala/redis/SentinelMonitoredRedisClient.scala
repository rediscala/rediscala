package redis

import org.apache.pekko.actor.*
import redis.commands.*

case class SentinelMonitoredRedisClient(
  sentinels: Seq[(String, Int)] = Seq(("localhost", 26379)),
  master: String,
  username: Option[String] = None,
  password: Option[String] = None,
  db: Option[Int] = None,
  name: String = "SMRedisClient"
)(using system: ActorSystem, redisDispatcher: RedisDispatcher = Redis.dispatcher)
    extends SentinelMonitoredRedisClientLike(system, redisDispatcher)
    with RedisCommands
    with Transactions {

  val redisClient: RedisClient = withMasterAddr((ip, port) => {
    RedisClient(ip, port, username, password, db, name)
  })
  override val onNewSlave: (String, Int) => Unit = (ip: String, port: Int) => {}
  override val onSlaveDown: (String, Int) => Unit = (ip: String, port: Int) => {}
}
