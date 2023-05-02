package redis.actors

import akka.actor._
import org.scalatest.wordspec.AnyWordSpecLike
import akka.util.ByteString
import redis.api.hashes.Hgetall
import redis.protocol.MultiBulk
import scala.concurrent.Await
import scala.concurrent.Promise
import scala.collection.mutable
import java.net.InetSocketAddress
import com.typesafe.config.ConfigFactory
import redis.Redis
import redis.Operation
import redis.api.connection.Ping
import akka.testkit._

class RedisReplyDecoderSpec
    extends TestKit(ActorSystem("testsystem", ConfigFactory.parseString("""akka.loggers = ["akka.testkit.TestEventListener"]""")))
    with AnyWordSpecLike
    with ImplicitSender {

  import scala.concurrent.duration._

  val timeout = 5.seconds.dilated

  "RedisReplyDecoder" should {
    "ok" in within(timeout) {
      val promise = Promise[String]()
      val operation = Operation(Ping, promise)
      val q = QueuePromises(mutable.Queue[Operation[_, _]]())
      q.queue.enqueue(operation)

      val redisReplyDecoder = TestActorRef[RedisReplyDecoder](Props(classOf[RedisReplyDecoder]).withDispatcher(Redis.dispatcher.name))

      assert(redisReplyDecoder.underlyingActor.queuePromises.isEmpty)

      redisReplyDecoder ! q
      awaitAssert(assert(redisReplyDecoder.underlyingActor.queuePromises.size == 1))

      redisReplyDecoder ! ByteString("+PONG\r\n")
      assert(Await.result(promise.future, timeout) == "PONG")

      awaitAssert(assert(redisReplyDecoder.underlyingActor.queuePromises.isEmpty))

      val promise2 = Promise[String]()
      val promise3 = Promise[String]()
      val op2 = Operation(Ping, promise2)
      val op3 = Operation(Ping, promise3)
      val q2 = QueuePromises(mutable.Queue[Operation[_, _]]())
      q2.queue.enqueue(op2)
      q2.queue.enqueue(op3)

      redisReplyDecoder ! q2
      awaitAssert(assert(redisReplyDecoder.underlyingActor.queuePromises.size == 2))

      redisReplyDecoder ! ByteString("+PONG\r\n+PONG\r\n")
      assert(Await.result(promise2.future, timeout) == "PONG")
      assert(Await.result(promise3.future, timeout) == "PONG")
      assert(redisReplyDecoder.underlyingActor.queuePromises.isEmpty)
    }

    "can't decode" in within(timeout) {
      val probeMock = TestProbe()

      val redisClientActor =
        TestActorRef[RedisClientActorMock2](Props(classOf[RedisClientActorMock2], probeMock.ref).withDispatcher(Redis.dispatcher.name))
      val promise = Promise[String]()
      redisClientActor ! Operation(Ping, promise)
      awaitAssert(
        {
          assert(redisClientActor.underlyingActor.queuePromises.length == 1)
          redisClientActor.underlyingActor.onWriteSent()
          assert(redisClientActor.underlyingActor.queuePromises.isEmpty)
        },
        timeout
      )

      EventFilter[Exception](occurrences = 1, start = "Redis Protocol error: Got 110 as initial reply byte").intercept {
        redisClientActor.underlyingActor.onDataReceived(ByteString("not valid redis reply"))
      }
      assert(probeMock.expectMsg("restartConnection") == "restartConnection")
      assertThrows[InvalidRedisReply.type](Await.result(promise.future, timeout))

      val promise2 = Promise[String]()
      redisClientActor ! Operation(Ping, promise2)
      awaitAssert(
        {
          assert(redisClientActor.underlyingActor.queuePromises.length == 1)
          redisClientActor.underlyingActor.onWriteSent()
          assert(redisClientActor.underlyingActor.queuePromises.isEmpty)
        },
        timeout
      )

      EventFilter[Exception](occurrences = 1, start = "Redis Protocol error: Got 110 as initial reply byte").intercept {
        redisClientActor.underlyingActor.onDataReceived(ByteString("not valid redis reply"))
      }
      assert(probeMock.expectMsg("restartConnection") == "restartConnection")
      assertThrows[InvalidRedisReply.type](Await.result(promise2.future, timeout))
    }

    "redis reply in many chunks" in within(timeout) {
      val promise1 = Promise[String]()
      val promise2 = Promise[String]()
      val promise3 = Promise[Map[String, String]]()
      val operation1 = Operation(Ping, promise1)
      val operation2 = Operation(Ping, promise2)
      val operation3 = Operation[MultiBulk, Map[String, String]](Hgetall[String, String]("key"), promise3)
      val q = QueuePromises(mutable.Queue[Operation[_, _]]())
      q.queue.enqueue(operation1)
      q.queue.enqueue(operation2)
      q.queue.enqueue(operation3)

      val redisReplyDecoder = TestActorRef[RedisReplyDecoder](Props(classOf[RedisReplyDecoder]).withDispatcher(Redis.dispatcher.name))

      assert(redisReplyDecoder.underlyingActor.queuePromises.isEmpty)

      redisReplyDecoder ! q
      awaitAssert(
        {
          assert(redisReplyDecoder.underlyingActor.queuePromises.size == 3)
        },
        timeout
      )

      redisReplyDecoder ! ByteString("+P")
      awaitAssert(redisReplyDecoder.underlyingActor.partiallyDecoded.rest == ByteString("+P"))
      awaitAssert(assert(redisReplyDecoder.underlyingActor.partiallyDecoded.isFullyDecoded == false))

      redisReplyDecoder ! ByteString("ONG\r")
      awaitAssert(redisReplyDecoder.underlyingActor.partiallyDecoded.rest == ByteString("+PONG\r"))

      redisReplyDecoder ! ByteString("\n+PONG2")
      awaitAssert(redisReplyDecoder.underlyingActor.partiallyDecoded.rest == ByteString("+PONG2"))

      assert(Await.result(promise1.future, timeout) == "PONG")

      redisReplyDecoder ! ByteString("\r\n")
      awaitAssert(assert(redisReplyDecoder.underlyingActor.partiallyDecoded.isFullyDecoded))
      awaitAssert(assert(redisReplyDecoder.underlyingActor.partiallyDecoded.rest.isEmpty))

      assert(Await.result(promise2.future, timeout) == "PONG2")

      awaitAssert(assert(redisReplyDecoder.underlyingActor.queuePromises.size == 1))

      val multibulkString0 = ByteString()
      val multibulkString = ByteString("*4\r\n$3\r\nfoo\r\n$3\r\nbar\r\n$5\r\nHello\r\n$5\r\nWorld\r\n")
      val (multibulkStringStart, multibulkStringEnd) = multibulkString.splitAt(multibulkString.length - 1)

      redisReplyDecoder ! multibulkString0
      awaitAssert(assert(redisReplyDecoder.underlyingActor.partiallyDecoded.isFullyDecoded), timeout)

      for {
        b <- multibulkStringStart
      } yield {
        redisReplyDecoder ! ByteString(b)
        awaitAssert(assert(redisReplyDecoder.underlyingActor.partiallyDecoded.isFullyDecoded == false), timeout)
      }
      redisReplyDecoder ! multibulkStringEnd

      awaitAssert(assert(redisReplyDecoder.underlyingActor.queuePromises.size == 0))
      awaitAssert(assert(redisReplyDecoder.underlyingActor.partiallyDecoded.isFullyDecoded), timeout)

      assert(Await.result(promise3.future, timeout) == Map("foo" -> "bar", "Hello" -> "World"))

      assert(redisReplyDecoder.underlyingActor.queuePromises.isEmpty)
    }
  }
}

class RedisClientActorMock2(probeMock: ActorRef)
    extends RedisClientActor(new InetSocketAddress("localhost", 6379), () => { Seq() }, (status: Boolean) => { () }, Redis.dispatcher.name) {
  override def preStart(): Unit = {
    // disable preStart of RedisWorkerIO
  }

  override def restartConnection(): Unit = {
    super.restartConnection()
    probeMock ! "restartConnection"
  }
}
