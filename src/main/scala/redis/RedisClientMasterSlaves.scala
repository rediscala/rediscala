package redis

import redis.RediscalaCompat.actor.ActorRef
import redis.RediscalaCompat.actor.ActorSystem
import redis.commands.Transactions
import redis.protocol.RedisReply
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class RedisClientMasterSlaves(master: RedisServer, slaves: Seq[RedisServer])(implicit
  _system: ActorSystem,
  redisDispatcher: RedisDispatcher = Redis.dispatcher
) extends RedisCommands
    with Transactions {
  implicit val executionContext: ExecutionContext = _system.dispatchers.lookup(redisDispatcher.name)

  val masterClient = RedisClient(master.host, master.port, master.username, master.password, master.db)

  val slavesClients = RedisClientPool(slaves)

  override def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    if (redisCommand.isMasterOnly || slaves.isEmpty) {
      masterClient.send(redisCommand)
    } else {
      slavesClients.send(redisCommand)
    }
  }

  def redisConnection: ActorRef = masterClient.redisConnection
}
