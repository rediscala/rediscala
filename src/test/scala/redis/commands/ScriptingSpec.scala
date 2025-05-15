package redis.commands

import java.io.File
import org.apache.pekko.util.ByteString
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.SpanSugar.*
import redis.*
import redis.actors.ReplyErrorException
import redis.api.scripting.RedisScript
import redis.protocol.Bulk
import redis.protocol.MultiBulk
import scala.concurrent.Await

class ScriptingSpec extends RedisDockerServer with ScalaFutures {

  "Scripting commands" should {
    val redisScript = RedisScript("return 'rediscala'")
    val redisScriptKeysArgs = RedisScript("return {KEYS[1],ARGV[1]}")
    val redisScriptConversionObject = RedisScript("return redis.call('get', 'dumbKey')")

    "evalshaOrEval (RedisScript)" in {
      assert(Await.result(redis.scriptFlush(), timeOut))
      val r = Await.result(redis.evalshaOrEval(redisScriptKeysArgs, Seq("key"), Seq("arg")), timeOut)
      assert(r == MultiBulk(Some(Vector(Bulk(Some(ByteString("key"))), Bulk(Some(ByteString("arg")))))))
    }

    "EVAL" in {
      assert(Await.result(redis.eval(redisScript.script), timeOut) == Bulk(Some(ByteString("rediscala"))))
    }

    "EVAL with type conversion" in {
      val dumbObject = new DumbClass("foo", "bar")
      val r = redis
        .set("dumbKey", dumbObject)
        .flatMap(_ => {
          redis.eval[DumbClass](redisScriptConversionObject.script)
        })

      assert(Await.result(r, timeOut) == dumbObject)
    }

    "EVALSHA" in {
      assert(Await.result(redis.evalsha(redisScript.sha1), timeOut) == Bulk(Some(ByteString("rediscala"))))
    }

    "EVALSHA with type conversion" in {
      val dumbObject = new DumbClass("foo2", "bar2")
      val r = redis
        .set("dumbKey", dumbObject)
        .flatMap(_ => {
          redis.evalsha[DumbClass](redisScriptConversionObject.sha1)
        })

      assert(Await.result(r, timeOut) == dumbObject)
    }

    "evalshaOrEvalForTypeOf (RedisScript)" in {
      assert(Await.result(redis.scriptFlush(), timeOut))
      val dumbObject = new DumbClass("foo3", "bar3")

      val r = redis
        .set("dumbKey", dumbObject)
        .flatMap(_ => {
          redis.evalshaOrEval[DumbClass](redisScriptConversionObject)
        })

      assert(Await.result(r, timeOut) == dumbObject)
    }

    "SCRIPT FLUSH" in {
      assert(Await.result(redis.scriptFlush(), timeOut))
    }

    "SCRIPT KILL" in {

      withRedisServer(serverPort => {
        val redisKiller = RedisClient(port = serverPort)
        val redisScriptLauncher = RedisClient(port = serverPort)
        assert(
          intercept[ReplyErrorException](Await.result(redisKiller.scriptKill(), timeOut)) == ReplyErrorException(
            "NOTBUSY No scripts in execution right now."
          )
        )

        // infinite script (5 seconds)
        val infiniteScript = redisScriptLauncher.eval("""
            |local i = 1
            |while(i > 0) do
            |end
            |return 0
          """.stripMargin)
        Thread.sleep(1000)
        assert(redisKiller.scriptKill().futureValue(Timeout(30.seconds)))
        val actual = intercept[ReplyErrorException](Await.result(infiniteScript, timeOut))
        assert(actual.message.contains("Script killed by user with SCRIPT KILL"), actual)
        assert(actual.message.contains("2817d960235dc23d2cea9cc2c716a0b123b56be8"), actual)
      })
    }

    "SCRIPT LOAD" in {
      assert(Await.result(redis.scriptLoad("return 'rediscala'"), timeOut) == "d4cf7650161a37eb55a7e9325f3534cec6fc3241")
    }

    "SCRIPT EXISTS" in {
      val redisScriptNotFound = RedisScript("return 'SCRIPT EXISTS not found'")
      val redisScriptFound = RedisScript("return 'SCRIPT EXISTS found'")
      val scriptsLoaded = redis.scriptLoad(redisScriptFound.script).flatMap(_ => redis.scriptExists(redisScriptFound.sha1, redisScriptNotFound.sha1))
      assert(Await.result(scriptsLoaded, timeOut) == Seq(true, false))

    }

    "fromFile" in {
      val testScriptFile = new File(getClass.getResource("/lua/test.lua").getPath)
      assert(RedisScript.fromFile(testScriptFile) == RedisScript("""return "test""""))
    }

    "fromResource" in {
      val testScriptPath = "/lua/test.lua"
      assert(RedisScript.fromResource(testScriptPath) == RedisScript("""return "test""""))
    }

  }
}
