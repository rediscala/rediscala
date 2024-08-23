package redis

import java.net.InetSocketAddress
import redis.RediscalaCompat.actor.*
import redis.actors.RedisClientActor
import scala.concurrent.*
import scala.concurrent.duration.FiniteDuration

abstract class RedisClientActorLike(system: ActorSystem, redisDispatcher: RedisDispatcher, connectTimeout: Option[FiniteDuration] = None)
    extends ActorRequest {
  var host: String
  var port: Int
  val name: String
  val username: Option[String] = None
  val password: Option[String] = None
  val db: Option[Int] = None
  implicit val executionContext: ExecutionContext = system.dispatchers.lookup(redisDispatcher.name)

  val redisConnection: ActorRef = system.actorOf(
    RedisClientActor
      .props(new InetSocketAddress(host, port), getConnectOperations, onConnectStatus, redisDispatcher.name, connectTimeout)
      .withDispatcher(redisDispatcher.name),
    name + '-' + Redis.tempName()
  )

  def reconnect(host: String = host, port: Int = port): Unit = {
    if (this.host != host || this.port != port) {
      this.host = host
      this.port = port
      redisConnection ! new InetSocketAddress(host, port)
    }
  }

  def onConnect(redis: RedisCommands): Unit = {
    (username, password) match {
      case (Some(username), Some(password)) => redis.auth(username, password)
      case (None, Some(password)) => redis.auth(password)
      case (_, _) =>
    }
    db.foreach(redis.select(_))
  }

  def onConnectStatus: Boolean => Unit = (status: Boolean) => {}

  def getConnectOperations: () => Seq[Operation[?, ?]] = () => {
    val self = this
    val redis = new BufferedRequest with RedisCommands {
      implicit val executionContext: ExecutionContext = self.executionContext
    }
    onConnect(redis)
    redis.operations.result()
  }

  /**
   * Disconnect from the server (stop the actor)
   */
  def stop(): Unit = {
    system stop redisConnection
  }
}
