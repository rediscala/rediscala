package redis.actors

import java.net.InetSocketAddress
import redis.RediscalaCompat.util.ByteString
import redis.api.connection.Auth
import redis.api.pubsub.*
import redis.protocol.Error
import redis.protocol.MultiBulk
import redis.protocol.RedisReply

abstract class RedisSubscriberActor(
  address: InetSocketAddress,
  channels: Seq[String],
  patterns: Seq[String],
  authUsername: Option[String] = None,
  authPassword: Option[String] = None,
  onConnectStatus: Boolean => Unit
) extends RedisWorkerIO(address, onConnectStatus)
    with DecodeReplies {
  def onConnectWrite(): ByteString = {
    (authUsername, authPassword) match {
      case (Some(username), Some(password)) => Auth(username, Some(password)).encodedRequest
      case (None, Some(password)) => Auth(password).encodedRequest
      case (_, _) => ByteString.empty
    }
  }

  def onMessage(m: Message): Unit

  def onPMessage(pm: PMessage): Unit

  /**
   * Keep states of channels and actor in case of connection reset
   */
  private[redis] var channelsSubscribed = channels.toSet
  private var patternsSubscribed = patterns.toSet

  override def preStart(): Unit = {
    super.preStart()
    if (channelsSubscribed.nonEmpty) {
      write(SUBSCRIBE(channelsSubscribed.toSeq*).toByteString)
    }
    if (patternsSubscribed.nonEmpty) {
      write(PSUBSCRIBE(patternsSubscribed.toSeq*).toByteString)
    }
  }

  def writing: Receive = { case message: SubscribeMessage =>
    if (message.params.nonEmpty) {
      write(message.toByteString)
      message match {
        case s: SUBSCRIBE => channelsSubscribed ++= s.channel
        case u: UNSUBSCRIBE => channelsSubscribed --= u.channel
        case ps: PSUBSCRIBE => patternsSubscribed ++= ps.pattern
        case pu: PUNSUBSCRIBE => patternsSubscribed --= pu.pattern
      }
    }
  }

  def subscribe(channels: String*): Unit = {
    self ! SUBSCRIBE(channels*)
  }

  def unsubscribe(channels: String*): Unit = {
    self ! UNSUBSCRIBE(channels*)
  }

  def psubscribe(patterns: String*): Unit = {
    self ! PSUBSCRIBE(patterns*)
  }

  def punsubscribe(patterns: String*): Unit = {
    self ! PUNSUBSCRIBE(patterns*)
  }

  def onConnectionClosed(): Unit = {}

  def onWriteSent(): Unit = {}

  def onDataReceived(dataByteString: ByteString): Unit = {
    decodeReplies(dataByteString)
  }

  def onDecodedReply(reply: RedisReply): Unit = {
    reply match {
      case MultiBulk(Some(list)) if list.length == 3 && list.head.toByteString.utf8String == "message" =>
        onMessage(Message(list(1).toByteString.utf8String, list(2).toByteString))
      case MultiBulk(Some(list)) if list.length == 4 && list.head.toByteString.utf8String == "pmessage" =>
        onPMessage(PMessage(list(1).toByteString.utf8String, list(2).toByteString.utf8String, list(3).toByteString))
      case error @ Error(_) =>
        onErrorReply(error)
      case _ => // subscribe or psubscribe
    }
  }

  def onDataReceivedOnClosingConnection(dataByteString: ByteString): Unit = decodeReplies(dataByteString)

  def onClosingConnectionClosed(): Unit = {}

  def onErrorReply(error: Error): Unit = {}
}
