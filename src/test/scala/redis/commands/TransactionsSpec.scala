package redis.commands

import redis._
import scala.concurrent.Await
import redis.RediscalaCompat.util.ByteString
import redis.actors.ReplyErrorException
import redis.protocol.Bulk
import redis.protocol.Status
import redis.protocol.MultiBulk

class TransactionsSpec extends RedisDockerServer {

  "Transactions commands" should {
    "basic" in {
      val redisTransaction = redis.transaction()
      redisTransaction.exec()
      redisTransaction.watch("a")
      val set = redisTransaction.set("a", "abc")
      val decr = redisTransaction.decr("a")
      val get = redisTransaction.get("a")
      redisTransaction.exec()
      val r = for {
        s <- set
        g <- get
      } yield {
        assert(s)
        assert(g == Some(ByteString("abc")))
      }
      assert(intercept[ReplyErrorException] { Await.result(decr, timeOut) }.getMessage == "ERR value is not an integer or out of range")
      Await.result(r, timeOut)
    }

    "function api" should {
      "empty" in {
        val empty = redis.multi().exec()
        assert(Await.result(empty, timeOut) == MultiBulk(Some(Vector())))
      }
      val redisTransaction = redis.multi(redis => {
        redis.set("a", "abc")
        redis.get("a")
      })
      val exec = redisTransaction.exec()
      "non empty" in {
        assert(Await.result(exec, timeOut) == MultiBulk(Some(Vector(Status(ByteString("OK")), Bulk(Some(ByteString("abc")))))))
      }
      "reused" in {
        redisTransaction.get("transactionUndefinedKey")
        val exec = redisTransaction.exec()
        assert(Await.result(exec, timeOut) == MultiBulk(Some(Vector(Status(ByteString("OK")), Bulk(Some(ByteString("abc"))), Bulk(None)))))
      }
      "watch" in {
        val transaction = redis.watch("transactionWatchKey")
        assert(transaction.watcher.result() == Set("transactionWatchKey"))
        transaction.unwatch()
        assert(transaction.watcher.result().isEmpty)
        val set = transaction.set("transactionWatch", "value")
        transaction.exec()
        val r = for {
          s <- set
        } yield {
          assert(s)
        }
        Await.result(r, timeOut)
      }
    }

  }
}
