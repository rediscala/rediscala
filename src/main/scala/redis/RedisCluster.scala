package redis

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.event.Logging
import redis.api.clusters.ClusterNode
import redis.api.clusters.ClusterSlot
import redis.protocol.RedisReply
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

case class RedisCluster(redisServers: Seq[RedisServer], name: String = "RedisClientPool")(using
  _system: ActorSystem,
  redisDispatcher: RedisDispatcher = Redis.dispatcher
) extends RedisClientPoolLike(_system, redisDispatcher)
    with RedisCommands {

  val log = Logging.getLogger(_system, this)

  val clusterSlotsRef: AtomicReference[Option[Map[ClusterSlot, RedisConnection]]] = new AtomicReference(
    Option.empty[Map[ClusterSlot, RedisConnection]]
  )
  val lockClusterSlots = new AtomicBoolean(true)

  override val redisServerConnections: collection.Map[RedisServer, RedisConnection] = {
    redisServers.map { server =>
      makeRedisConnection(server, defaultActive = true)
    }.toMap
  }
  refreshConnections()

  def equalsHostPort(clusterNode: ClusterNode, server: RedisServer) = {
    clusterNode.host == server.host && clusterNode.port == server.port
  }

  override def onConnectStatus(server: RedisServer, active: AtomicBoolean): Boolean => Unit = { (status: Boolean) =>
    {
      if (active.compareAndSet(!status, status)) {
        refreshConnections()
      }

      clusterSlotsRef.get.map { clusterSlots =>
        if (clusterSlots.keys.exists(cs => equalsHostPort(cs.master, server))) {
          log.info("one master is still dead => refresh clusterSlots")
          asyncRefreshClusterSlots()
        }
      }

    }
  }

  def getClusterSlots(): Future[Map[ClusterSlot, RedisConnection]] = {

    def resolveClusterSlots(retry: Int): Future[Map[ClusterSlot, RedisConnection]] = {
      clusterSlots().map { clusterSlots =>
        clusterSlots.flatMap { clusterSlot =>
          val maybeServerConnection = redisServerConnections.find { case (server, _) => equalsHostPort(clusterSlot.master, server) }
          maybeServerConnection.map { case (_, redisConnection) => (clusterSlot, redisConnection) }
        }.toMap
      }.recoverWith { case e =>
        if (retry - 1 == 0) {
          Future.failed(e)
        } else {
          resolveClusterSlots(retry - 1)
        }
      }
    }
    resolveClusterSlots(3) // retry 3 times
  }

  def asyncRefreshClusterSlots(force: Boolean = false): Future[Unit] = {
    if (force || lockClusterSlots.compareAndSet(false, true)) {
      try {
        getClusterSlots().map { clusterSlot =>
          log.info("refreshClusterSlots: " + clusterSlot.toString())
          clusterSlotsRef.set(Some(clusterSlot))
          lockClusterSlots.compareAndSet(true, false)
          ()
        }.recoverWith { case NonFatal(e) =>
          log.error("refreshClusterSlots:", e)
          lockClusterSlots.compareAndSet(true, false)
          Future.failed(e)
        }
      } catch {
        case NonFatal(e) =>
          lockClusterSlots.compareAndSet(true, false)
          throw e
      }
    } else {

      Future.successful(clusterSlotsRef.get)
    }
  }

  protected def send[T](redisConnection: ActorRef, redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {
    val promise = Promise[T]()
    redisConnection ! Operation(redisCommand, promise)
    promise.future
  }

  def getRedisConnection(slot: Int): Option[RedisConnection] = {
    getClusterAndConnection(slot).map { case (_, redisConnection) => redisConnection }

  }

  def getClusterAndConnection(slot: Int): Option[(ClusterSlot, RedisConnection)] = {
    clusterSlotsRef.get.flatMap {
      _.find { case (clusterSlot, _) =>
        val result = clusterSlot.begin <= slot && slot <= clusterSlot.end
        if (result) {
          log.debug(s"slot $slot => " + clusterSlot.master.toString)
        }
        result
      }
    }
  }

  val redirectMessagePattern = """(MOVED|ASK) \d+ (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d+)""".r
  override def send[T](redisCommand: RedisCommand[? <: RedisReply, T]): Future[T] = {

    val maybeRedisActor: Option[ActorRef] = getRedisActor(redisCommand)

    maybeRedisActor.map { redisConnection =>
      send(redisConnection, redisCommand).recoverWith {
        case e: redis.actors.ReplyErrorException if e.message.startsWith("MOVED") || e.message.startsWith("ASK") =>
          e.message match {
            // follow the redirect
            case redirectMessagePattern(opt, host, port) =>
              log.debug("Redirect:" + e.message)

              if (opt == "MOVED") {
                redisCommand match {
                  case _: ClusterKey => asyncRefreshClusterSlots()
                  case _ => log.info(s"Command do not implement ClusterKey : ${redisCommand}")
                }
              }

              redisServerConnections.find { case (server, redisConnection) =>
                server.host == host && server.port.toString == port && redisConnection.active.get
              }.map { case (_, redisConnection) =>
                send(redisConnection.actor, redisCommand)
              }.getOrElse(Future.failed(new Exception(s"server not found: $host:$port")))

            case _ => Future.failed(new Exception("bad exception format:" + e.message))
          }
        case error => Future.failed(error)
      }

    }.getOrElse(Future.failed(new RuntimeException("server not found: no server available")))
  }

  def getRedisActor[T](redisCommand: RedisCommand[? <: RedisReply, T]): Option[ActorRef] = {
    redisCommand match {
      case clusterKey: ClusterKey =>
        getRedisConnection(clusterKey.getSlot()).filter { _.active.get }.map(_.actor)
      case _ =>
        val redisActors = redisConnectionPool
        if (redisActors.nonEmpty) {
          // if it is not a cluster command => random connection
          // TODO use RoundRobinPoolRequest
          Some(redisActors(ThreadLocalRandom.current().nextInt(redisActors.length)))
        } else {
          None
        }
    }
  }

  def groupByClusterServer(keys: Seq[String]): Seq[Seq[String]] = {
    keys.groupBy { key =>
      getRedisConnection(RedisComputeSlot.hashSlot(key))
    }.values.toSeq
  }

  Await.result(asyncRefreshClusterSlots(force = true), Duration(10, TimeUnit.SECONDS))
}
