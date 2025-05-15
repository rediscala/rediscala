package redis.actors

import java.net.InetSocketAddress
import org.apache.pekko.actor.*
import org.apache.pekko.testkit.*
import org.apache.pekko.util.ByteString
import org.scalatest.wordspec.AnyWordSpecLike
import redis.Operation
import redis.Redis
import redis.api.connection.Ping
import redis.api.strings.Get
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.Promise

class RedisClientActorSpec extends TestKit(ActorSystem()) with AnyWordSpecLike with ImplicitSender {

  import scala.concurrent.duration.*

  val getConnectOperations: () => Seq[Operation[?, ?]] = () => {
    Seq()
  }

  val timeout = 120.seconds.dilated

  val onConnectStatus: Boolean => Unit = (status: Boolean) => {}

  "RedisClientActor" should {

    "ok" in within(timeout) {
      val probeReplyDecoder = TestProbe()
      val probeMock = TestProbe()

      val promiseConnect1 = Promise[String]()
      val opConnectPing = Operation(Ping, promiseConnect1)
      val promiseConnect2 = Promise[Option[ByteString]]()
      val getCmd = Get("key")
      val opConnectGet = Operation(getCmd, promiseConnect2)

      val getConnectOperations: () => Seq[Operation[?, ?]] = () => {
        Seq(opConnectPing, opConnectGet)
      }

      val redisClientActor = TestActorRef[RedisClientActorMock](
        Props(classOf[RedisClientActorMock], probeReplyDecoder.ref, probeMock.ref, getConnectOperations, onConnectStatus)
          .withDispatcher(Redis.dispatcher.name)
      )

      val promise = Promise[String]()
      val op1 = Operation(Ping, promise)
      redisClientActor ! op1
      val promise2 = Promise[String]()
      val op2 = Operation(Ping, promise2)
      redisClientActor ! op2

      assert(probeMock.expectMsg(WriteMock) == WriteMock)
      awaitAssert(assert(redisClientActor.underlyingActor.queuePromises.length == 2))

      // onConnectWrite
      redisClientActor.underlyingActor.onConnectWrite()
      awaitAssert(assert(redisClientActor.underlyingActor.queuePromises.toSeq == Seq(opConnectPing, opConnectGet, op1, op2)))
      awaitAssert(assert(redisClientActor.underlyingActor.queuePromises.length == 4))

      // onWriteSent
      redisClientActor.underlyingActor.onWriteSent()
      assert(probeReplyDecoder.expectMsgType[QueuePromises] == QueuePromises(mutable.Queue(opConnectPing, opConnectGet, op1, op2)))
      awaitAssert(assert(redisClientActor.underlyingActor.queuePromises.isEmpty))

      // onDataReceived
      awaitAssert(redisClientActor.underlyingActor.onDataReceived(ByteString.empty))
      assert(probeReplyDecoder.expectMsgType[ByteString] == ByteString.empty)

      awaitAssert(redisClientActor.underlyingActor.onDataReceived(ByteString("bytestring")))
      assert(probeReplyDecoder.expectMsgType[ByteString] == ByteString("bytestring"))

      // onConnectionClosed
      val deathWatcher = TestProbe()
      deathWatcher.watch(probeReplyDecoder.ref)
      redisClientActor.underlyingActor.onConnectionClosed()
      assert(deathWatcher.expectTerminated(probeReplyDecoder.ref, 30.seconds).isInstanceOf[Terminated])
    }

    "onConnectionClosed with promises queued" in {
      val probeReplyDecoder = TestProbe()
      val probeMock = TestProbe()

      val redisClientActor = TestActorRef[RedisClientActorMock](
        Props(classOf[RedisClientActorMock], probeReplyDecoder.ref, probeMock.ref, getConnectOperations, onConnectStatus).withDispatcher(
          Redis.dispatcher.name
        )
      ).underlyingActor

      val promise3 = Promise[String]()
      redisClientActor.receive(Operation(Ping, promise3))
      assert(redisClientActor.queuePromises.length == 1)

      val deathWatcher = TestProbe()
      deathWatcher.watch(probeReplyDecoder.ref)

      redisClientActor.onConnectionClosed()
      assert(deathWatcher.expectTerminated(probeReplyDecoder.ref, 30.seconds).isInstanceOf[Terminated])
      assertThrows[NoConnectionException.type](Await.result(promise3.future, 10.seconds))
    }

    "replyDecoder died -> reset connection" in {
      val probeReplyDecoder = TestProbe()
      val probeMock = TestProbe()

      val redisClientActorRef = TestActorRef[RedisClientActorMock](
        Props(classOf[RedisClientActorMock], probeReplyDecoder.ref, probeMock.ref, getConnectOperations, onConnectStatus)
          .withDispatcher(Redis.dispatcher.name)
      )
      val redisClientActor = redisClientActorRef.underlyingActor

      val promiseSent = Promise[String]()
      val promiseNotSent = Promise[String]()
      val operation = Operation(Ping, promiseSent)
      redisClientActor.receive(operation)
      assert(redisClientActor.queuePromises.length == 1)

      redisClientActor.onWriteSent()
      assert(redisClientActor.queuePromises.isEmpty)
      assert(probeReplyDecoder.expectMsgType[QueuePromises] == QueuePromises(mutable.Queue(operation)))

      redisClientActor.receive(Operation(Ping, promiseNotSent))
      assert(redisClientActor.queuePromises.length == 1)

      val deathWatcher = TestProbe()
      deathWatcher.watch(probeReplyDecoder.ref)
      deathWatcher.watch(redisClientActorRef)

      probeReplyDecoder.ref ! Kill
      assert(deathWatcher.expectTerminated(probeReplyDecoder.ref).isInstanceOf[Terminated])
      assert(redisClientActor.queuePromises.length == 1)
    }
  }
}

class RedisClientActorMock(
  probeReplyDecoder: ActorRef,
  probeMock: ActorRef,
  getConnectOperations: () => Seq[Operation[?, ?]],
  onConnectStatus: Boolean => Unit
) extends RedisClientActor(new InetSocketAddress("localhost", 6379), getConnectOperations, onConnectStatus, Redis.dispatcher.name) {
  override def initRepliesDecoder() = probeReplyDecoder

  override def preStart(): Unit = {
    // disable preStart of RedisWorkerIO
  }

  override def write(byteString: ByteString): Unit = {
    probeMock ! WriteMock
  }
}

object WriteMock
