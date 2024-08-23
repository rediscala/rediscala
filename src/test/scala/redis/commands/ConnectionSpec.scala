package redis.commands

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.actors.ReplyErrorException
import scala.concurrent.Await

class ConnectionSpec extends RedisDockerServer {

  "Connection commands" should {
    "AUTH" in {
      val expectMessage =
        "ERR AUTH <password> called without any password configured for the default user. Are you sure your configuration is correct?"
      assert(intercept[ReplyErrorException] { Await.result(redis.auth("no password"), timeOut) }.getMessage == expectMessage)
    }
    "AUTH with bad username and password" in {
      val errorMessage = "WRONGPASS invalid username-password pair or user is disabled."
      assert(intercept[ReplyErrorException] {
        Await.result(redis.auth(username = "baduser", password = "bad password"), timeOut)
      }.getMessage == errorMessage)
    }
    "ECHO" in {
      val hello = "Hello World!"
      assert(Await.result(redis.echo(hello), timeOut) == Some(ByteString(hello)))
    }
    "PING" in {
      assert(Await.result(redis.ping(), timeOut) == "PONG")
    }
    "QUIT" in {
      // todo test that the TCP connection is reset.
      val f = redis.quit()
      Thread.sleep(1000)
      val ping = redis.ping()
      assert(Await.result(f, timeOut))
      assert(Await.result(ping, timeOut) == "PONG")
    }
    "SELECT" in {
      assert(Await.result(redis.select(1), timeOut))
      assert(Await.result(redis.select(0), timeOut))
      assert(intercept[ReplyErrorException] { Await.result(redis.select(-1), timeOut) }.getMessage == "ERR DB index is out of range")
      assert(intercept[ReplyErrorException] { Await.result(redis.select(1000), timeOut) }.getMessage == "ERR DB index is out of range")
    }
    "SWAPDB" in {
      assert(Await.result(redis.select(0), timeOut))
      assert(Await.result(redis.set("key1", "value1"), timeOut))
      assert(Await.result(redis.select(1), timeOut))
      assert(Await.result(redis.set("key2", "value2"), timeOut))
      assert(Await.result(redis.swapdb(0, 1), timeOut))
      assert(Await.result(redis.get("key1"), timeOut) == Some(ByteString("value1")))
      assert(Await.result(redis.select(0), timeOut))
      assert(Await.result(redis.get("key2"), timeOut) == Some(ByteString("value2")))
    }
  }
}
