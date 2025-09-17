package redis.actors

import java.net.InetSocketAddress
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.Props
import org.apache.pekko.io.Tcp.*
import org.apache.pekko.testkit.*
import org.apache.pekko.util.ByteString
import org.scalatest.wordspec.AnyWordSpecLike
import redis.Redis

class RedisWorkerIOSpec extends TestKit(ActorSystem()) with AnyWordSpecLike with ImplicitSender {

  import scala.concurrent.duration.*

  val timeout = 120.seconds.dilated

  "RedisWorkerIO" should {

    val address = new InetSocketAddress("localhost", 6379)
    "connect CommandFailed then reconnect" in within(timeout) {
      val probeTcp = TestProbe()
      val probeMock = TestProbe()

      val redisWorkerIO = TestActorRef[RedisWorkerIOMock](
        Props(classOf[RedisWorkerIOMock], probeTcp.ref, address, probeMock.ref, ByteString.empty).withDispatcher(Redis.dispatcher.name)
      )

      val connectMsg = probeTcp.expectMsgType[Connect]
      assert(connectMsg == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      probeTcp.reply(CommandFailed(connectMsg))
      assert(probeMock.expectMsg(OnConnectionClosed) == OnConnectionClosed)

      // should reconnect in 2s
      within(1.second, 4.seconds) {
        val connectMsg = probeTcp.expectMsgType[Connect]
        assert(connectMsg == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
        assert(connectMsg.remoteAddress ne address)

        val probeTcpWorker = TestProbe()
        probeTcpWorker.send(redisWorkerIO, Connected(connectMsg.remoteAddress, address))

        assert(probeTcpWorker.expectMsgType[Register] == Register(redisWorkerIO))
      }
    }

    "ok" in within(timeout) {
      val probeTcp = TestProbe()
      val probeMock = TestProbe()

      val redisWorkerIO = TestActorRef[RedisWorkerIOMock](
        Props(classOf[RedisWorkerIOMock], probeTcp.ref, address, probeMock.ref, ByteString.empty).withDispatcher(Redis.dispatcher.name)
      )

      redisWorkerIO ! "PING1"

      val connectMsg = probeTcp.expectMsgType[Connect]
      assert(connectMsg == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker = TestProbe()
      probeTcpWorker.send(redisWorkerIO, Connected(connectMsg.remoteAddress, address))

      assert(probeTcpWorker.expectMsgType[Register] == Register(redisWorkerIO))

      assert(probeTcpWorker.expectMsgType[Write] == Write(ByteString("PING1"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)

      redisWorkerIO ! "PING2"
      redisWorkerIO ! "PING3"
      probeTcpWorker.reply(WriteAck)
      assert(probeTcpWorker.expectMsgType[Write] == Write(ByteString("PING2PING3"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)

      redisWorkerIO ! "PING"
      probeTcpWorker.expectNoMessage(1.seconds)
      probeTcpWorker.send(redisWorkerIO, WriteAck)
      assert(probeTcpWorker.expectMsgType[Write] == Write(ByteString("PING"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)
    }

    "reconnect : connected <-> disconnected" in within(timeout) {
      val probeTcp = TestProbe()
      val probeMock = TestProbe()

      val redisWorkerIO = TestActorRef[RedisWorkerIOMock](
        Props(classOf[RedisWorkerIOMock], probeTcp.ref, address, probeMock.ref, ByteString.empty).withDispatcher(Redis.dispatcher.name)
      )

      redisWorkerIO ! "PING1"

      val connectMsg = probeTcp.expectMsgType[Connect]
      assert(connectMsg == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker = TestProbe()
      probeTcpWorker.send(redisWorkerIO, Connected(connectMsg.remoteAddress, address))

      assert(probeTcpWorker.expectMsgType[Register] == Register(redisWorkerIO))

      assert(probeTcpWorker.expectMsgType[Write] == Write(ByteString("PING1"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)

      redisWorkerIO ! "PING 2"
      awaitAssert(assert(redisWorkerIO.underlyingActor.bufferWrite.result() == ByteString("PING 2")))
      // ConnectionClosed
      probeTcpWorker.send(redisWorkerIO, ErrorClosed("test"))
      assert(probeMock.expectMsg(OnConnectionClosed) == OnConnectionClosed)
      awaitAssert(assert(redisWorkerIO.underlyingActor.bufferWrite.length == 0))

      // Reconnect
      val connectMsg2 = probeTcp.expectMsgType[Connect]
      assert(connectMsg2 == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker2 = TestProbe()
      probeTcpWorker2.send(redisWorkerIO, Connected(connectMsg2.remoteAddress, address))
      assert(probeTcpWorker2.expectMsgType[Register] == Register(redisWorkerIO))

      redisWorkerIO ! "PING1"
      assert(probeTcpWorker2.expectMsgType[Write] == Write(ByteString("PING1"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)
    }

    "onConnectedCommandFailed" in within(timeout) {
      val probeTcp = TestProbe()
      val probeMock = TestProbe()

      val redisWorkerIO = TestActorRef[RedisWorkerIOMock](
        Props(classOf[RedisWorkerIOMock], probeTcp.ref, address, probeMock.ref, ByteString.empty).withDispatcher(Redis.dispatcher.name)
      )

      redisWorkerIO ! "PING1"

      val connectMsg = probeTcp.expectMsgType[Connect]
      assert(connectMsg == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker = TestProbe()
      probeTcpWorker.send(redisWorkerIO, Connected(connectMsg.remoteAddress, address))

      assert(probeTcpWorker.expectMsgType[Register] == Register(redisWorkerIO))

      val msg = probeTcpWorker.expectMsgType[Write]
      assert(msg == Write(ByteString("PING1"), WriteAck))

      probeTcpWorker.reply(CommandFailed(msg))
      assert(probeTcpWorker.expectMsgType[Write] == Write(ByteString("PING1"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)
    }

    "received" in within(timeout) {
      val probeTcp = TestProbe()
      val probeMock = TestProbe()

      val redisWorkerIO = TestActorRef[RedisWorkerIOMock](
        Props(classOf[RedisWorkerIOMock], probeTcp.ref, address, probeMock.ref, ByteString.empty).withDispatcher(Redis.dispatcher.name)
      )

      redisWorkerIO ! "PING1"

      val connectMsg = probeTcp.expectMsgType[Connect]
      assert(connectMsg == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker = TestProbe()
      probeTcpWorker.send(redisWorkerIO, Connected(connectMsg.remoteAddress, address))

      assert(probeTcpWorker.expectMsgType[Register] == Register(redisWorkerIO))

      assert(probeTcpWorker.expectMsgType[Write] == Write(ByteString("PING1"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)

      probeTcpWorker.send(redisWorkerIO, Received(ByteString("PONG")))
      assert(probeMock.expectMsgType[ByteString] == ByteString("PONG"))
    }

    "Address Changed" in within(timeout) {
      val probeTcp = TestProbe()
      val probeMock = TestProbe()

      val redisWorkerIO = TestActorRef[RedisWorkerIOMock](
        Props(classOf[RedisWorkerIOMock], probeTcp.ref, address, probeMock.ref, ByteString.empty).withDispatcher(Redis.dispatcher.name)
      )

      redisWorkerIO ! "PING1"

      val connectMsg = probeTcp.expectMsgType[Connect]
      assert(connectMsg == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker = TestProbe()
      probeTcpWorker.send(redisWorkerIO, Connected(connectMsg.remoteAddress, address))

      assert(probeTcpWorker.expectMsgType[Register] == Register(redisWorkerIO))

      assert(probeTcpWorker.expectMsgType[Write] == Write(ByteString("PING1"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)
      probeTcpWorker.reply(WriteAck)

      // change address
      val address2 = new InetSocketAddress("localhost", 6380)
      redisWorkerIO ! address2

      assert(probeMock.expectMsg(OnConnectionClosed) == OnConnectionClosed)

      redisWorkerIO ! "PING2"

      val connectMsg2 = probeTcp.expectMsgType[Connect]
      assert(connectMsg2 == Connect(address2, options = SO.KeepAlive(on = true) :: Nil))

      val probeTcpWorker2 = TestProbe()
      probeTcpWorker2.send(redisWorkerIO, Connected(connectMsg.remoteAddress, address))

      assert(probeTcpWorker2.expectMsgType[Register] == Register(redisWorkerIO))

      assert(probeTcpWorker2.expectMsgType[Write] == Write(ByteString("PING2"), WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)
      probeTcpWorker2.reply(WriteAck)

      // receiving data on connection with the sending direction closed
      probeTcpWorker.send(redisWorkerIO, Received(ByteString("PONG1")))
      assert(probeMock.expectMsg(DataReceivedOnClosingConnection) == DataReceivedOnClosingConnection)

      // receiving data on open connection
      probeTcpWorker2.send(redisWorkerIO, Received(ByteString("PONG2")))
      assert(probeMock.expectMsgType[ByteString] == ByteString("PONG2"))

      // close connection
      probeTcpWorker.send(redisWorkerIO, ConfirmedClosed)
      assert(probeMock.expectMsg(ClosingConnectionClosed) == ClosingConnectionClosed)
    }

    "on connect write" in within(timeout) {
      val probeTcp = TestProbe()
      val probeMock = TestProbe()
      val onConnectByteString = ByteString("on connect write")

      val redisWorkerIO = TestActorRef[RedisWorkerIOMock](
        Props(classOf[RedisWorkerIOMock], probeTcp.ref, address, probeMock.ref, onConnectByteString).withDispatcher(Redis.dispatcher.name)
      )

      val connectMsg = probeTcp.expectMsgType[Connect]
      assert(connectMsg == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker = TestProbe()
      probeTcpWorker.send(redisWorkerIO, Connected(connectMsg.remoteAddress, address))

      assert(probeTcpWorker.expectMsgType[Register] == Register(redisWorkerIO))

      assert(probeTcpWorker.expectMsgType[Write] == Write(onConnectByteString, WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)

      redisWorkerIO ! "PING1"
      awaitAssert(assert(redisWorkerIO.underlyingActor.bufferWrite.result() == ByteString("PING1")))

      // ConnectionClosed
      probeTcpWorker.send(redisWorkerIO, ErrorClosed("test"))
      assert(probeMock.expectMsg(OnConnectionClosed) == OnConnectionClosed)

      awaitAssert(assert(redisWorkerIO.underlyingActor.bufferWrite.length == 0))

      // Reconnect
      val connectMsg2 = probeTcp.expectMsgType[Connect]
      assert(connectMsg2 == Connect(address, options = SO.KeepAlive(on = true) :: Nil))
      val probeTcpWorker2 = TestProbe()
      probeTcpWorker2.send(redisWorkerIO, Connected(connectMsg2.remoteAddress, address))
      assert(probeTcpWorker2.expectMsgType[Register] == Register(redisWorkerIO))

      assert(probeTcpWorker2.expectMsgType[Write] == Write(onConnectByteString, WriteAck))
      assert(probeMock.expectMsg(WriteSent) == WriteSent)
    }
  }
}

class RedisWorkerIOMock(probeTcp: ActorRef, address: InetSocketAddress, probeMock: ActorRef, _onConnectWrite: ByteString)
    extends RedisWorkerIO(address, (status: Boolean) => { () }) {
  override val tcp = probeTcp

  def writing: Receive = { case s: String =>
    write(ByteString(s))
  }

  def onConnectionClosed(): Unit = {
    probeMock ! OnConnectionClosed
  }

  def onDataReceived(dataByteString: ByteString): Unit = {
    probeMock ! dataByteString
  }

  def onWriteSent(): Unit = {
    probeMock ! WriteSent
  }

  def onConnectWrite(): ByteString = _onConnectWrite

  def onDataReceivedOnClosingConnection(dataByteString: ByteString): Unit = probeMock ! DataReceivedOnClosingConnection

  def onClosingConnectionClosed(): Unit = probeMock ! ClosingConnectionClosed
}

object WriteSent

object OnConnectionClosed

object DataReceivedOnClosingConnection

object ClosingConnectionClosed
