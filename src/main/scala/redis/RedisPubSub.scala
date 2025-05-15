package redis

import java.net.InetSocketAddress
import org.apache.pekko.actor.*
import redis.actors.RedisSubscriberActorWithCallback
import redis.api.pubsub.*

case class RedisPubSub(
  host: String = "localhost",
  port: Int = 6379,
  channels: Seq[String],
  patterns: Seq[String],
  onMessage: Message => Unit = _ => {},
  onPMessage: PMessage => Unit = _ => {},
  authUsername: Option[String] = None,
  authPassword: Option[String] = None,
  name: String = "RedisPubSub"
)(implicit system: ActorRefFactory, redisDispatcher: RedisDispatcher = Redis.dispatcher) {

  val redisConnection: ActorRef = system.actorOf(
    Props(
      classOf[RedisSubscriberActorWithCallback],
      new InetSocketAddress(host, port),
      channels,
      patterns,
      onMessage,
      onPMessage,
      authUsername,
      authPassword,
      onConnectStatus()
    ).withDispatcher(redisDispatcher.name),
    name + '-' + Redis.tempName()
  )

  /**
   * Disconnect from the server (stop the actor)
   */
  def stop(): Unit = {
    system stop redisConnection
  }

  def subscribe(channels: String*): Unit = {
    redisConnection ! SUBSCRIBE(channels*)
  }

  def unsubscribe(channels: String*): Unit = {
    redisConnection ! UNSUBSCRIBE(channels*)
  }

  def psubscribe(patterns: String*): Unit = {
    redisConnection ! PSUBSCRIBE(patterns*)
  }

  def punsubscribe(patterns: String*): Unit = {
    redisConnection ! PUNSUBSCRIBE(patterns*)
  }

  def onConnectStatus(): Boolean => Unit = (status: Boolean) => {}
}
