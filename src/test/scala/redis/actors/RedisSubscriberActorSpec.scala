package redis.actors

import redis.RediscalaTestCompat.testkit.*
import redis.RediscalaCompat.actor.*
import org.scalatest.wordspec.AnyWordSpecLike
import java.net.InetSocketAddress
import redis.RediscalaCompat.util.ByteString
import redis.protocol.RedisProtocolRequest
import redis.Redis
import redis.RediscalaCompat.io.Tcp.*
import redis.api.pubsub.Message
import redis.api.pubsub.PMessage

class RedisSubscriberActorSpec extends TestKit(ActorSystem()) with AnyWordSpecLike with ImplicitSender {

  import scala.concurrent.duration.*

  "RedisClientActor" should {

    "connection closed -> reconnect" in {
      val probeMock = TestProbe()
      val channels = Seq("channel")
      val patterns = Seq("pattern.*")

      val subscriberActor = TestActorRef[SubscriberActor](
        Props(classOf[SubscriberActor], new InetSocketAddress("localhost", 6379), channels, patterns, probeMock.ref)
          .withDispatcher(Redis.dispatcher.name)
      )

      val connectMsg = probeMock.expectMsgType[Connect]
      assert(connectMsg == Connect(subscriberActor.underlyingActor.address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker = TestProbe()
      probeTcpWorker.send(subscriberActor, Connected(connectMsg.remoteAddress, connectMsg.remoteAddress))
      assert(probeTcpWorker.expectMsgType[Register] == Register(subscriberActor))
      val bs = RedisProtocolRequest
        .multiBulk("SUBSCRIBE", channels.map(ByteString(_))) ++ RedisProtocolRequest.multiBulk("PSUBSCRIBE", patterns.map(ByteString(_)))
      assert(probeTcpWorker.expectMsgType[Write] == Write(bs, WriteAck))
      probeTcpWorker.reply(WriteAck)

      val newChannels = channels :+ "channel2"
      subscriberActor.underlyingActor.subscribe("channel2")
      awaitAssert(
        {
          assert(subscriberActor.underlyingActor.channelsSubscribed.toSet == newChannels.toSet)
        },
        5.seconds.dilated
      )
      assert(probeTcpWorker.expectMsgType[Write] == Write(RedisProtocolRequest.multiBulk("SUBSCRIBE", Seq(ByteString("channel2"))), WriteAck))
      probeTcpWorker.reply(WriteAck)

      // ConnectionClosed
      probeTcpWorker.send(subscriberActor, ErrorClosed("test"))

      // Reconnect
      val connectMsg2 = probeMock.expectMsgType[Connect]
      assert(connectMsg2 == Connect(subscriberActor.underlyingActor.address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker2 = TestProbe()
      probeTcpWorker2.send(subscriberActor, Connected(connectMsg2.remoteAddress, connectMsg2.remoteAddress))
      assert(probeTcpWorker2.expectMsgType[Register] == Register(subscriberActor))

      // check the new Channel is there
      val bs2 = RedisProtocolRequest
        .multiBulk("SUBSCRIBE", newChannels.map(ByteString(_))) ++ RedisProtocolRequest.multiBulk("PSUBSCRIBE", patterns.map(ByteString(_)))
      val m = probeTcpWorker2.expectMsgType[Write]
      assert(m == Write(bs2, WriteAck))
    }
  }
}

class SubscriberActor(address: InetSocketAddress, channels: Seq[String], patterns: Seq[String], probeMock: ActorRef)
    extends RedisSubscriberActor(address, channels, patterns, None, None, (status: Boolean) => { () }) {

  override val tcp = probeMock

  override def onMessage(m: Message) = {
    probeMock ! m
  }

  def onPMessage(pm: PMessage): Unit = {
    probeMock ! pm
  }
}
