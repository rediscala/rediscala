package redis.commands

import redis._
import redis.protocol.Status
import scala.concurrent.Await
import akka.util.ByteString
import redis.actors.ReplyErrorException
import java.io.InputStream
import java.io.OutputStream
import scala.io.Source
import scala.sys.process._

class ConnectionSpec extends RedisDockerServer {

  sequential

  "Connection commands" should {
    "AUTH" in {
      val expectMessage =
        "ERR AUTH <password> called without any password configured for the default user. Are you sure your configuration is correct?"
      Await.result(redis.auth("no password"), timeOut) must throwA[ReplyErrorException](expectMessage)
    }
    "AUTH with username and password" in {
      // TODO When we support ACL command, use ACL to create an user instead of redis-cli
      val command = s"${RedisServerHelper.redisServerPath}/redis-cli -h ${redis.host} -p ${redis.port}"
      val username = "testuser"
      val password = "password0"
      println(command)
      Process(command)
        .run(
          new ProcessIO(
            (writeInput: OutputStream) => {
              //
              Thread.sleep(2000)
              println(s"acl setuser ${username} on >${password} allcommands allkeys")
              writeInput.write(s"acl setuser ${username} on >${password} allcommands allkeys\n".getBytes)
              writeInput.flush
              writeInput.write("exit\n".getBytes)
              writeInput.flush
            },
            (processOutput: InputStream) => {
              Source.fromInputStream(processOutput).getLines().foreach { l => println(l) }
            },
            (processError: InputStream) => {
              Source.fromInputStream(processError).getLines().foreach { l => println(l) }
            },
            daemonizeThreads = false
          )
        )
        .exitValue()
      Thread.sleep(5000)
      Await.result(redis.auth(username = username, password = password), timeOut).toByteString mustEqual Status.okByteString
    }
    "AUTH with bad username and password" in {
      val errorMessage = "WRONGPASS invalid username-password pair or user is disabled"
      Await.result(redis.auth(username = "baduser", password = "bad password"), timeOut) must throwA[ReplyErrorException](errorMessage)
    }
    "ECHO" in {
      val hello = "Hello World!"
      Await.result(redis.echo(hello), timeOut) mustEqual Some(ByteString(hello))
    }
    "PING" in {
      Await.result(redis.ping(), timeOut) mustEqual "PONG"
    }
    "QUIT" in {
      // todo test that the TCP connection is reset.
      val f = redis.quit()
      Thread.sleep(1000)
      val ping = redis.ping()
      Await.result(f, timeOut) mustEqual true
      Await.result(ping, timeOut) mustEqual "PONG"
    }
    "SELECT" in {
      Await.result(redis.select(1), timeOut) mustEqual true
      Await.result(redis.select(0), timeOut) mustEqual true
      Await.result(redis.select(-1), timeOut) must throwA[ReplyErrorException]("ERR DB index is out of range")
      Await.result(redis.select(1000), timeOut) must throwA[ReplyErrorException]("ERR DB index is out of range")
    }
    "SWAPDB" in {
      Await.result(redis.select(0), timeOut) mustEqual true
      Await.result(redis.set("key1", "value1"), timeOut) mustEqual true
      Await.result(redis.select(1), timeOut) mustEqual true
      Await.result(redis.set("key2", "value2"), timeOut) mustEqual true
      Await.result(redis.swapdb(0, 1), timeOut) mustEqual true
      Await.result(redis.get("key1"), timeOut) mustEqual Some(ByteString("value1"))
      Await.result(redis.select(0), timeOut) mustEqual true
      Await.result(redis.get("key2"), timeOut) mustEqual Some(ByteString("value2"))
    }
  }
}
