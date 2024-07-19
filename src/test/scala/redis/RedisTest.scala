package redis

import redis.RediscalaCompat.ConfigurationException
import scala.concurrent.*
import redis.RediscalaCompat.util.ByteString

class RedisTest extends RedisDockerServer {

  "basic test" should {
    "ping" in {
      assert(Await.result(redis.ping(), timeOut) == "PONG")
    }
    "set" in {
      assert(Await.result(redis.set("key", "value"), timeOut))
    }
    "get" in {
      assert(Await.result(redis.get("key"), timeOut) == Some(ByteString("value")))
    }
    "del" in {
      assert(Await.result(redis.del("key"), timeOut) == 1)
    }
    "get not found" in {
      assert(Await.result(redis.get("key"), timeOut) == None)
    }
  }

  "init connection test" should {
    "ok" in {
      withRedisServer(port => {
        val redis = RedisClient(port = port)
        // TODO set password (CONFIG SET requiredpass password)
        val r = for {
          _ <- redis.select(2)
          _ <- redis.set("keyDbSelect", "2")
        } yield {
          val redis = RedisClient(port = port, password = Some("password"), db = Some(2))
          assert(Await.result(redis.get[String]("keyDbSelect"), timeOut) == Some("2"))
        }
        Await.result(r, timeOut)
      })
    }
    "use custom dispatcher" in {
      def test() = withRedisServer(port => {
        implicit val redisDispatcher = RedisDispatcher("no-this-dispatcher")
        RedisClient(port = port)
      })
      assertThrows[ConfigurationException] { test() }
    }
  }

}
