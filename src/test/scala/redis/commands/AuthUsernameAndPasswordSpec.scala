package redis.commands

import redis.*
import redis.protocol.Status
import scala.concurrent.Await
import java.io.InputStream
import java.io.OutputStream
import scala.io.Source
import scala.sys.process.*

class AuthUsernameAndPasswordSpec extends RedisStandaloneServer {

  "AUTH with username and password" should {
    "ok" in {
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
      assert(Await.result(redis.auth(username = username, password = password), timeOut).toByteString == Status.okByteString)
    }
  }
}
