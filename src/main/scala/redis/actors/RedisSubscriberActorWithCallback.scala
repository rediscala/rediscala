package redis.actors

import java.net.InetSocketAddress
import redis.api.pubsub.*

class RedisSubscriberActorWithCallback(
  address: InetSocketAddress,
  channels: Seq[String],
  patterns: Seq[String],
  messageCallback: Message => Unit,
  pmessageCallback: PMessage => Unit,
  authUsername: Option[String] = None,
  authPassword: Option[String] = None,
  onConnectStatus: Boolean => Unit
) extends RedisSubscriberActor(address, channels, patterns, authUsername, authPassword, onConnectStatus) {
  def onMessage(m: Message) = messageCallback(m)

  def onPMessage(pm: PMessage) = pmessageCallback(pm)
}
