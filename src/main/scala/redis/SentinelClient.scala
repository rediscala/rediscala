package redis

import java.net.InetSocketAddress
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.Props
import org.apache.pekko.event.Logging
import redis.actors.RedisSubscriberActorWithCallback
import redis.api.pubsub.Message
import redis.api.pubsub.PMessage

case class SentinelClient(
  var host: String = "localhost",
  var port: Int = 26379,
  onMasterChange: (String, String, Int) => Unit = (masterName: String, ip: String, port: Int) => {},
  onNewSentinel: (String, String, Int) => Unit = (masterName: String, sentinelip: String, sentinelport: Int) => {},
  onSentinelDown: (String, String, Int) => Unit = (masterName: String, sentinelip: String, sentinelport: Int) => {},
  onNewSlave: (String, String, Int) => Unit = (masterName: String, sentinelip: String, sentinelport: Int) => {},
  onSlaveDown: (String, String, Int) => Unit = (masterName: String, sentinelip: String, sentinelport: Int) => {},
  name: String = "SentinelClient"
)(using _system: ActorSystem, redisDispatcher: RedisDispatcher = Redis.dispatcher)
    extends RedisClientActorLike(_system, redisDispatcher)
    with SentinelCommands {
  val system: ActorSystem = _system

  val log = Logging.getLogger(system, this)

  val channels = Seq("+switch-master", "+sentinel", "+sdown", "-sdown", "+failover-state-send-slaveof-noone", "+slave")

  val onMessage = (message: Message) => {

    if (log.isDebugEnabled)
      log.debug(s"SentinelClient.onMessage: message received:${message.channel} ${message.data.utf8String}")

    message match {
      case Message("+switch-master", data) =>
        data.utf8String.split(" ") match {
          case Array(master, oldip, oldport, newip, newport) =>
            onMasterChange(master, newip, newport.toInt)
          case _ =>
        }
      case Message("+failover-state-send-slaveof-noone", data) =>
        data.utf8String.split(" ") match {
          case Array("slave", slaveName, slaveip, slaveport, "@", master, masterip, masterport) =>
            onMasterChange(master, slaveip, slaveport.toInt)
          case _ =>
        }
      case Message("+sentinel", data) =>
        data.utf8String.split(" ") match {
          case Array("sentinel", sentName, sentinelip, sentinelport, "@", master, masterip, masterport) =>
            onNewSentinel(master, sentinelip, sentinelport.toInt)
          case _ =>
        }
      case Message("+sdown", data) =>
        data.utf8String.split(" ") match {
          case Array("sentinel", sentName, sentinelip, sentinelport, "@", master, masterip, masterport) =>
            onSentinelDown(master, sentinelip, sentinelport.toInt)

          case Array("slave", slaveName, slaveip, slaveport, "@", master, masterip, masterport) =>
            onSlaveDown(master, slaveip, slaveport.toInt)

          case _ =>
        }
      case Message("-sdown", data) =>
        data.utf8String.split(" ") match {
          case Array("slave", slaveName, slaveip, slaveport, "@", master, masterip, masterport) =>
            onNewSlave(master, slaveip, slaveport.toInt)

          case _ =>
        }
      case Message("+slave", data) =>
        data.utf8String.split(" ") match {
          case Array("slave", slaveName, slaveip, slaveport, "@", master, masterip, masterport) =>
            onNewSlave(master, slaveip, slaveport.toInt)

          case _ =>
        }
      case _ =>
        log.warning(s"SentinelClient.onMessage: unexpected message received: $message")
    }
  }

  val redisPubSubConnection: ActorRef = system.actorOf(
    Props(
      classOf[RedisSubscriberActorWithCallback],
      new InetSocketAddress(host, port),
      channels,
      Seq(),
      onMessage,
      (pmessage: PMessage) => {},
      None,
      None,
      (status: Boolean) => {}
    ).withDispatcher(redisDispatcher.name),
    name + '-' + Redis.tempName()
  )

  /**
    * Disconnect from the server (stop the actors)
    */
  override def stop(): Unit = {
    system stop redisConnection
    system stop redisPubSubConnection
  }

}
