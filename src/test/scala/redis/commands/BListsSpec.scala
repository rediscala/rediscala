package redis.commands

import redis.*
import scala.concurrent.Await
import redis.RediscalaCompat.util.ByteString
import scala.concurrent.duration.*

class BListsSpec extends RedisDockerServer {

  "Blocking Lists commands" should {
    "BLPOP" should {
      "already containing elements" in {
        val redisB = RedisBlockingClient(port = port)
        val r = for {
          _ <- redis.del("blpop1", "blpop2")
          p <- redis.rpush("blpop1", "a", "b", "c")
          b <- redisB.blpop(Seq("blpop1", "blpop2"))
        } yield {
          assert(b == Some("blpop1" -> ByteString("a")))
        }
        val rr = Await.result(r, timeOut)
        redisB.stop()
        rr
      }

      "blocking" in {
        val redisB = RedisBlockingClient(port = port)
        val rr = within(1.seconds, 10.seconds) {
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
        redisB.stop()
        rr
      }

      "blocking timeout" in {
        val redisB = RedisBlockingClient(port = port)
        val rr = within(1.seconds, 10.seconds) {
          val r = redis
            .del("blpopBlockTimeout")
            .flatMap(_ => {
              redisB.brpop(Seq("blpopBlockTimeout"), 1.seconds)
            })
          assert(Await.result(r, timeOut).isEmpty)
        }
        redisB.stop()
        rr
      }
    }

    "BRPOP" should {
      "already containing elements" in {
        val redisB = RedisBlockingClient(port = port)
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

      "blocking" in {
        val redisB = RedisBlockingClient(port = port)
        val rr = within(1.seconds, 10.seconds) {
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
        redisB.stop()
        rr
      }

      "blocking timeout" in {
        val redisB = RedisBlockingClient(port = port)
        val rr = within(1.seconds, 10.seconds) {
          val r = redis
            .del("brpopBlockTimeout")
            .flatMap(_ => {
              redisB.brpop(Seq("brpopBlockTimeout"), 1.seconds)
            })
          assert(Await.result(r, timeOut).isEmpty)
        }
        redisB.stop()
        rr
      }
    }

    "BRPOPLPUSH" should {
      "already containing elements" in {
        val redisB = RedisBlockingClient(port = port)
        val r = for {
          _ <- redis.del("brpopplush1", "brpopplush2")
          p <- redis.rpush("brpopplush1", "a", "b", "c")
          b <- redisB.brpoplpush("brpopplush1", "brpopplush2")
        } yield {
          assert(b == Some(ByteString("c")))
        }
        val rr = Await.result(r, timeOut)
        redisB.stop()
        rr
      }

      "blocking" in {
        val redisB = RedisBlockingClient(port = port)
        val rr = within(1.seconds, 10.seconds) {
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
        redisB.stop()
        rr
      }

      "blocking timeout" in {
        val redisB = RedisBlockingClient(port = port)
        val rr = within(1.seconds, 10.seconds) {
          val r = redis
            .del("brpopplushBlockTimeout1", "brpopplushBlockTimeout2")
            .flatMap(_ => {
              redisB.brpoplpush("brpopplushBlockTimeout1", "brpopplushBlockTimeout2", 1.seconds)
            })
          assert(Await.result(r, timeOut).isEmpty)
        }
        redisB.stop()
        rr
      }
    }
  }
}
