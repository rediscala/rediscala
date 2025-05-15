package redis.commands

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.ListDirection
import scala.concurrent.Await
import scala.concurrent.duration.*

class BListsSpec extends RedisDockerServer {

  private def withBlockingClient[A](f: RedisBlockingClient => A): A = {
    val redisB = RedisBlockingClient(port = port)
    try {
      f(redisB)
    } finally {
      redisB.stop()
    }
  }

  "Blocking Lists commands" should {
    "BLPOP" should {
      "already containing elements" in withBlockingClient { redisB =>
        val r = for {
          _ <- redis.del("blpop1", "blpop2")
          p <- redis.rpush("blpop1", "a", "b", "c")
          b <- redisB.blpop(Seq("blpop1", "blpop2"))
        } yield {
          assert(b == Some("blpop1" -> ByteString("a")))
        }
        Await.result(r, timeOut)
      }

      "blocking" in withBlockingClient { redisB =>
        within(1.seconds, 10.seconds) {
          val r = redis
            .del("blpopBlock")
            .flatMap(_ => {
              val blpop = redisB.blpop(Seq("blpopBlock"))
              Thread.sleep(1000)
              redis.rpush("blpopBlock", "a", "b", "c")
              blpop
            })
          assert(Await.result(r, timeOut) == Some("blpopBlock" -> ByteString("a")))
        }
      }

      "blocking timeout" in withBlockingClient { redisB =>
        val redisB = RedisBlockingClient(port = port)
        within(1.seconds, 10.seconds) {
          val r = redis
            .del("blpopBlockTimeout")
            .flatMap(_ => {
              redisB.brpop(Seq("blpopBlockTimeout"), 1.seconds)
            })
          assert(Await.result(r, timeOut).isEmpty)
        }
      }
    }

    "BRPOP" should {
      "already containing elements" in withBlockingClient { redisB =>
        val r = for {
          _ <- redis.del("brpop1", "brpop2")
          p <- redis.rpush("brpop1", "a", "b", "c")
          b <- redisB.brpop(Seq("brpop1", "brpop2"))
        } yield {
          redisB.stop()
          assert(b == Some("brpop1" -> ByteString("c")))
        }
        Await.result(r, timeOut)
      }

      "blocking" in withBlockingClient { redisB =>
        within(1.seconds, 10.seconds) {
          val r = redis
            .del("brpopBlock")
            .flatMap(_ => {
              val brpop = redisB.brpop(Seq("brpopBlock"))
              Thread.sleep(1000)
              redis.rpush("brpopBlock", "a", "b", "c")
              brpop
            })
          assert(Await.result(r, timeOut) == Some("brpopBlock" -> ByteString("c")))
        }
      }

      "blocking timeout" in withBlockingClient { redisB =>
        within(1.seconds, 10.seconds) {
          val r = redis
            .del("brpopBlockTimeout")
            .flatMap(_ => {
              redisB.brpop(Seq("brpopBlockTimeout"), 1.seconds)
            })
          assert(Await.result(r, timeOut).isEmpty)
        }
      }
    }

    "BRPOPLPUSH" should {
      "already containing elements" in withBlockingClient { redisB =>
        val r = for {
          _ <- redis.del("brpopplush1", "brpopplush2")
          p <- redis.rpush("brpopplush1", "a", "b", "c")
          b <- redisB.brpoplpush("brpopplush1", "brpopplush2")
        } yield {
          assert(b == Some(ByteString("c")))
        }
        Await.result(r, timeOut)
      }

      "blocking" in withBlockingClient { redisB =>
        within(1.seconds, 10.seconds) {
          val r = redis
            .del("brpopplushBlock1", "brpopplushBlock2")
            .flatMap(_ => {
              val brpopplush = redisB.brpoplpush("brpopplushBlock1", "brpopplushBlock2")
              Thread.sleep(1000)
              redis.rpush("brpopplushBlock1", "a", "b", "c")
              brpopplush
            })
          assert(Await.result(r, timeOut) == Some(ByteString("c")))
        }
      }

      "blocking timeout" in withBlockingClient { redisB =>
        within(1.seconds, 10.seconds) {
          val r = redis
            .del("brpopplushBlockTimeout1", "brpopplushBlockTimeout2")
            .flatMap(_ => {
              redisB.brpoplpush("brpopplushBlockTimeout1", "brpopplushBlockTimeout2", 1.seconds)
            })
          assert(Await.result(r, timeOut).isEmpty)
        }
      }
    }

    "BLMOVE" in withBlockingClient { redisB =>
      val r = for {
        _ <- redis.del("lmove_key_1")
        _ <- redis.del("lmove_key_2")
        _ <- redis.rpush("lmove_key_1", "one", "two", "three")
        res1 <- redisB.blmove("lmove_key_1", "lmove_key_2", ListDirection.Right, ListDirection.Left)
        res2 <- redisB.blmove("lmove_key_1", "lmove_key_2", ListDirection.Left, ListDirection.Right)
        res3 <- redis.lrange("lmove_key_1", 0, -1)
        res4 <- redis.lrange("lmove_key_2", 0, -1)
      } yield {
        assert(res1 == Option(ByteString("three")))
        assert(res2 == Option(ByteString("one")))
        assert(res3 == Seq(ByteString("two")))
        assert(res4 == Seq("three", "one").map(ByteString.apply))
      }
      Await.result(r, timeOut)
    }
  }
}
