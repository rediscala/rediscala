package redis

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.actor.ActorSystem
import redis.commands.Transactions
import redis.protocol.RedisReply
import scala.concurrent.Future

case class SentinelMonitoredRedisClientMasterSlaves(sentinels: Seq[(String, Int)] = Seq(("localhost", 26379)), master: String)(implicit
  _system: ActorSystem,
  redisDispatcher: RedisDispatcher = Redis.dispatcher
) extends SentinelMonitored(_system, redisDispatcher)
    with ActorRequest
    with RedisCommands
    with Transactions {

  val masterClient: RedisClient = withMasterAddr((ip, port) => {
    RedisClient(ip, port, name = "SMRedisClient")
  })

  val slavesClients: RedisClientMutablePool = withSlavesAddr(slavesHostPort => {
    val slaves = slavesHostPort.map { case (ip, port) =>
      RedisServer(ip, port)
    }
    RedisClientMutablePool(slaves, name = "SMRedisClient")
  })

  val onNewSlave: (String, Int) => Unit = (ip: String, port: Int) => {
    log.info(s"onNewSlave $ip:$port")
    slavesClients.addServer(RedisServer(ip, port))
  }

  val onSlaveDown: (String, Int) => Unit = (ip: String, port: Int) => {
    log.info(s"onSlaveDown $ip:$port")
    slavesClients.removeServer(RedisServer(ip, port))
  }

  val onMasterChange: (String, Int) => Unit = (ip: String, port: Int) => {
    log.info(s"onMasterChange $ip:$port")
    masterClient.reconnect(ip, port)
  }

  /**
   * Disconnect from the server (stop the actors)
   */
  def stop(): Unit = {
    masterClient.stop()
    slavesClients.stop()
    sentinelClients.values.foreach(_.stop())
  }

  def redisConnection: ActorRef = masterClient.redisConnection

  override def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    if (redisCommand.isMasterOnly || slavesClients.redisConnectionPool.isEmpty) {
      masterClient.send(redisCommand)
    } else {
      slavesClients.send(redisCommand)
    }
  }
}
