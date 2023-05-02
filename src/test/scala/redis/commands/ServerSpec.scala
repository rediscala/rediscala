package redis.commands

import redis._
import scala.concurrent.Await
import redis.actors.InvalidRedisReply
import redis.actors.ReplyErrorException
import redis.api.NOSAVE

class ServerSpec extends RedisStandaloneServer {

  "Server commands" should {

    "BGSAVE" in {
      assert(Await.result(redis.bgsave(), timeOut) == "Background saving started")
    }

    "CLIENT KILL" in {
      assert(intercept[ReplyErrorException] { Await.result(redis.clientKill("8.8.8.8", 53), timeOut) }.getMessage == "ERR No such client")
    }

    "CLIENT LIST" in {
      val list: Seq[Map[String, String]] = Await.result(redis.clientList(), timeOut)
      assert(list.nonEmpty)
    }

    "CLIENT GETNAME" in {
      assert(Await.result(redis.clientGetname(), timeOut) == None)
    }

    "CLIENT SETNAME" in {
      assert(Await.result(redis.clientSetname("rediscala"), timeOut))
    }

    "CONFIG GET" in {
      val map: Map[String, String] = Await.result(redis.configGet("*"), timeOut)
      assert(map.nonEmpty)

    }
    "CONFIG SET" in {
      val r = for {
        set <- redis.configSet("loglevel", "warning")
        loglevel <- redis.configGet("loglevel")
      } yield {
        assert(set)
        assert(loglevel.get("loglevel") == Some("warning"))
      }
      Await.result(r, timeOut)
    }

    "CONFIG RESETSTAT" in {
      assert(Await.result(redis.configResetstat(), timeOut))
    }

    "DBSIZE" in {
      assert(Await.result(redis.dbsize(), timeOut) >= 0L)
    }

    "DEBUG OBJECT" in {
      assert(intercept[ReplyErrorException] { Await.result(redis.debugObject("serverDebugObj"), timeOut) }.getMessage == "ERR no such key")
    }

    "DEBUG SEGFAULT" in {
      pending
    }

    "FLUSHALL" in {
      assert(Await.result(redis.flushall(), timeOut))
    }

    "FLUSHALL ASYNC" in {
      assert(Await.result(redis.flushall(async = true), timeOut))
    }

    "FLUSHDB" in {
      assert(Await.result(redis.flushdb(), timeOut))
    }

    "FLUSHDB ASYNC" in {
      assert(Await.result(redis.flushdb(async = true), timeOut))
    }

    "INFO" in {
      val r = for {
        info <- redis.info()
        infoCpu <- redis.info("cpu")
      } yield {
        assert(info != null)
        assert(infoCpu != null)
      }
      Await.result(r, timeOut)
    }

    "LASTSAVE" in {
      assert(Await.result(redis.lastsave(), timeOut) >= 0L)
    }

    "SAVE" in {
      try {
        assert(Await.result(redis.save(), timeOut))
      } catch {
        case e: ReplyErrorException =>
          assert(e == ReplyErrorException("ERR Background save already in progress"))
      }
    }

    "SLAVE OF" in {
      assert(Await.result(redis.slaveof("server", 12345), timeOut))
    }

    "SLAVE OF NO ONE" in {
      assert(Await.result(redis.slaveofNoOne(), timeOut))
    }

    "TIME" in {
      val result: (Long, Long) = Await.result(redis.time(), timeOut)
      assert(result != null)
    }

    "BGREWRITEAOF" in {
      // depending on the redis version, this string could vary, redis 2.8.21 says 'scheduled'
      // but redis 2.8.18 says 'started'
      val r = Await.result(redis.bgrewriteaof(), timeOut)
      assert(
        Seq(
          "Background append only file rewriting started",
          "Background append only file rewriting scheduled"
        ).contains(r)
      )
    }

    "SHUTDOWN" in {
      assertThrows[InvalidRedisReply.type](Await.result(redis.shutdown(), timeOut))
    }

    "SHUTDOWN (with modifier)" in {
      withRedisServer(port => {
        val redis = RedisClient(port = port)
        assertThrows[InvalidRedisReply.type](Await.result(redis.shutdown(NOSAVE), timeOut))
      })
    }

  }
}
