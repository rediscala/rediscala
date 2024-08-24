package redis

import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import redis.RediscalaCompat.actor.ActorRef
import redis.RediscalaCompat.actor.ActorSystem
import redis.actors.RedisClientActor
import redis.protocol.RedisReply
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

abstract class RedisClientPoolLike(system: ActorSystem, redisDispatcher: RedisDispatcher) {

  def redisServerConnections: scala.collection.Map[RedisServer, RedisConnection]

  val name: String
  implicit val executionContext: ExecutionContext = system.dispatchers.lookup(redisDispatcher.name)

  private val redisConnectionRef: AtomicReference[Seq[ActorRef]] = new AtomicReference(Seq.empty)

  /**
    *
    * @return behave nicely with Future helpers like firstCompletedOf or sequence
    */
  def broadcast[T](redisCommand: RedisCommand[? <: RedisReply, T]): Seq[Future[T]] = {
    redisConnectionPool.map(redisConnection => {
      send(redisConnection, redisCommand)
    })
  }

  protected def send[T](redisConnection: ActorRef, redisCommand: RedisCommand[? <: RedisReply, T]): Future[T]

  def getConnectionsActive: Seq[ActorRef] = {
    redisServerConnections.collect {
      case (redisServer, redisConnection) if redisConnection.active.get => redisConnection.actor
    }.toVector
  }

  def redisConnectionPool: Seq[ActorRef] = {
    redisConnectionRef.get
  }

  def onConnect(redis: RedisCommands, server: RedisServer): Unit = {
    (server.username, server.password) match {
      case (Some(username), Some(password)) => redis.auth(username, password)
      case (None, Some(password)) => redis.auth(password)
      case (_, _) =>
    } // TODO log on auth failure
    server.db.foreach(redis.select)
  }

  def onConnectStatus(server: RedisServer, active: AtomicBoolean): Boolean => Unit = { (status: Boolean) =>
    {
      if (active.compareAndSet(!status, status)) {
        refreshConnections()
      }
    }
  }

  def refreshConnections(): Unit = {
    val actives = getConnectionsActive
    redisConnectionRef.set(actives)
  }

  def getConnectOperations(server: RedisServer): () => Seq[Operation[?, ?]] = () => {
    val self = this
    val redis = new BufferedRequest with RedisCommands {
      implicit val executionContext: ExecutionContext = self.executionContext
    }
    onConnect(redis, server)
    redis.operations.result()
  }

  /**
   * Disconnect from the server (stop the actor)
   */
  def stop(): Unit = {
    redisConnectionPool.foreach { redisConnection =>
      system stop redisConnection
    }
  }

  def makeRedisConnection(server: RedisServer, defaultActive: Boolean = false): (RedisServer, RedisConnection) = {
    val active = new AtomicBoolean(defaultActive)
    (server, RedisConnection(makeRedisClientActor(server, active), active))
  }

  def makeRedisClientActor(server: RedisServer, active: AtomicBoolean): ActorRef = {
    system.actorOf(
      RedisClientActor
        .props(new InetSocketAddress(server.host, server.port), getConnectOperations(server), onConnectStatus(server, active), redisDispatcher.name)
        .withDispatcher(redisDispatcher.name),
      name + '-' + Redis.tempName()
    )
  }

}
