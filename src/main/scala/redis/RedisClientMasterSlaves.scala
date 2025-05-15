package redis

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.actor.ActorSystem
import redis.commands.Transactions
import redis.protocol.RedisReply
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class RedisClientMasterSlaves(master: RedisServer, slaves: Seq[RedisServer])(using
  _system: ActorSystem,
  redisDispatcher: RedisDispatcher = Redis.dispatcher
) extends RedisCommands
    with Transactions {
  given executionContext: ExecutionContext = _system.dispatchers.lookup(redisDispatcher.name)

  val masterClient: RedisClient = RedisClient(master.host, master.port, master.username, master.password, master.db)

  val slavesClients: RedisClientPool = RedisClientPool(slaves)

  override def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    if (redisCommand.isMasterOnly || slaves.isEmpty) {
      masterClient.send(redisCommand)
    } else {
      slavesClients.send(redisCommand)
    }
  }

  def redisConnection: ActorRef = masterClient.redisConnection
}
