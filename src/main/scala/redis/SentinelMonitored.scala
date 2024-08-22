package redis

import redis.RediscalaCompat.actor.ActorSystem
import redis.RediscalaCompat.event.Logging
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

abstract class SentinelMonitored(system: ActorSystem, redisDispatcher: RedisDispatcher) {
  val sentinels: Seq[(String, Int)]
  val master: String
  val onMasterChange: (String, Int) => Unit
  val onNewSlave: (String, Int) => Unit
  val onSlaveDown: (String, Int) => Unit

  implicit val executionContext: ExecutionContext = system.dispatchers.lookup(redisDispatcher.name)

  val log = Logging.getLogger(system, this)

  val sentinelClients =
    collection.mutable.Map(
      sentinels.map(hp => (makeSentinelClientKey(hp._1, hp._2), makeSentinelClient(hp._1, hp._2)))*
    )

  def makeSentinelClientKey(host: String, port: Int) = s"$host:$port"

  def internalOnNewSlave(masterName: String, ip: String, port: Int): Unit = {
    if (master == masterName)
      onNewSlave(ip, port)
  }

  def internalOnSlaveDown(masterName: String, ip: String, port: Int): Unit = {
    if (master == masterName)
      onSlaveDown(ip, port)
  }

  def onSwitchMaster(masterName: String, ip: String, port: Int): Unit = {
    if (master == masterName) {
      onMasterChange(ip, port)
      onSlaveDown(ip, port)
    }
  }

  def makeSentinelClient(host: String, port: Int): SentinelClient = {
    SentinelClient(host, port, onSwitchMaster, onNewSentinel, onSentinelDown, internalOnNewSlave, internalOnSlaveDown, "SMSentinelClient")(system)
  }

  def onNewSentinel(masterName: String, sentinelip: String, sentinelport: Int): Unit = {
    val k = makeSentinelClientKey(sentinelip, sentinelport)
    if (master == masterName && !sentinelClients.contains(k)) {
      sentinelClients.synchronized {
        if (!sentinelClients.contains(k))
          sentinelClients += k -> makeSentinelClient(sentinelip, sentinelport)
      }
    }
  }

  def onSentinelDown(masterName: String, sentinelip: String, sentinelport: Int): Unit = {
    val k = makeSentinelClientKey(sentinelip, sentinelport)
    if (master == masterName && sentinelClients.contains(k)) {
      sentinelClients.synchronized {
        if (sentinelClients.contains(k)) {
          sentinelClients(k).stop()
          sentinelClients -= k
        }
      }
    }
  }

  def withMasterAddr[T](initFunction: (String, Int) => T): T = {
    import scala.concurrent.duration.*

    val f = sentinelClients.values.map(_.getMasterAddr(master))
    val ff = Future.sequence(f).map { listAddr =>
      listAddr.flatten.headOption.map { case (ip: String, port: Int) =>
        initFunction(ip, port)
      }.getOrElse(throw new Exception(s"No such master '$master'"))
    }

    Await.result(ff, 15.seconds)
  }

  def withSlavesAddr[T](initFunction: Seq[(String, Int)] => T): T = {
    import scala.concurrent.duration.*

    val fslaves = Future.sequence(sentinelClients.values.map(_.slaves(master))).map { lm =>
      val ipPortBuilder = Set.newBuilder[(String, Int)]
      for {
        slaves <- lm
        slave <- slaves
        ip <- slave.get("ip")
        port <- slave.get("port")
      } yield {
        ipPortBuilder += ip -> port.toInt
      }
      initFunction(ipPortBuilder.result().toSeq)
    }

    Await.result(fslaves, 15.seconds)
  }
}
