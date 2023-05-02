package redis

import scala.concurrent._
import redis.api.connection.Select
import scala.concurrent.duration._

class RedisPoolSpec extends RedisDockerServer {

  "basic pool test" should {
    "ok" in {
      val redisPool =
        RedisClientPool(Seq(RedisServer(port = port, db = Some(0)), RedisServer(port = port, db = Some(1)), RedisServer(port = port, db = Some(3))))
      val key = "keyPoolDb0"
      redisPool.set(key, 0)
      val r = for {
        getDb1 <- redisPool.get(key)
        getDb2 <- redisPool.get(key)
        getDb0 <- redisPool.get[String](key)
        select <- Future.sequence(redisPool.broadcast(Select(0)))
        getKey1 <- redisPool.get[String](key)
        getKey2 <- redisPool.get[String](key)
        getKey0 <- redisPool.get[String](key)
      } yield {
        assert(getDb1.isEmpty)
        assert(getDb2.isEmpty)
        assert(getDb0 == Some("0"))
        assert(select == Seq(true, true, true))
        assert(getKey1 == Some("0"))
        assert(getKey2 == Some("0"))
        assert(getKey0 == Some("0"))
      }
      Await.result(r, timeOut)
    }

    "check status" in {
      val redisPool =
        RedisClientPool(Seq(RedisServer(port = port, db = Some(0)), RedisServer(port = port, db = Some(1)), RedisServer(port = 3333, db = Some(3))))
      val key = "keyPoolDb0"

      awaitAssert(assert(redisPool.redisConnectionPool.size == 2), 20.second)
      redisPool.set(key, 0)
      val r = for {
        getDb1 <- redisPool.get(key)
        getDb0 <- redisPool.get[String](key)
        select <- Future.sequence(redisPool.broadcast(Select(0)))
        getKey1 <- redisPool.get[String](key)
        getKey0 <- redisPool.get[String](key)
      } yield {
        assert(getDb1.isEmpty)
        assert(getDb0 == Some("0"))
        assert(select == Seq(true, true))
        assert(getKey1 == Some("0"))
        assert(getKey0 == Some("0"))
      }
      Await.result(r, timeOut)

    }
  }
}
