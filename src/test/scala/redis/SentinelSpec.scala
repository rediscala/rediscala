package redis

import scala.concurrent._
import scala.concurrent.duration._
import redis.RediscalaTestCompat.testkit._
import org.scalatest.concurrent.ScalaFutures

class SentinelSpec extends RedisSentinelClients("SentinelSpec") with ScalaFutures {

  "sentinel monitored test" should {

    "master auto failover" in {
      val port = sentinelMonitoredRedisClient.redisClient.port

      awaitAssert(
        {
          assert(Await.result(sentinelMonitoredRedisClient.ping(), timeOut) == "PONG")
          assert(sentinelClient.failover(masterName).futureValue)
        },
        30.seconds.dilated
      )

      awaitAssert(
        {
          assert(Await.result(sentinelMonitoredRedisClient.ping(), timeOut) == "PONG")
          assert(
            Seq(slavePort1, slavePort2, port).contains(
              sentinelMonitoredRedisClient.redisClient.port
            )
          )
        },
        30.seconds.dilated
      )

      awaitAssert(
        {
          assert(Await.result(sentinelClient.failover(masterName), timeOut))
        },
        30.seconds.dilated
      )

      awaitAssert(
        {
          assert(Await.result(sentinelMonitoredRedisClient.ping(), timeOut) == "PONG")
        },
        30.seconds.dilated
      )
      assert(Seq(slavePort1, slavePort2, masterPort, port).contains(sentinelMonitoredRedisClient.redisClient.port))
    }

    "ping" in {
      assert(Await.result(sentinelMonitoredRedisClient.ping(), timeOut) == "PONG")
      assert(Await.result(redisClient.ping(), timeOut) == "PONG")
    }

    "sentinel nodes auto discovery" in {
      val sentinelCount = sentinelMonitoredRedisClient.sentinelClients.size
      val sentinel = newSentinelProcess()

      awaitAssert(assert(sentinelMonitoredRedisClient.sentinelClients.size == sentinelCount + 1), 10.second)

      sentinel.stop()
      awaitAssert(
        {
          assert(sentinelMonitoredRedisClient.sentinelClients.size == sentinelCount)
        },
        10.seconds
      )
    }
  }

  "sentinel test" should {
    "masters" in {
      val r = Await.result(sentinelClient.masters(), timeOut)
      assert(r(0)("name") == masterName)
      assert(r(0)("flags").startsWith("master"))
    }
    "no such master" in {
      val opt = Await.result(sentinelClient.getMasterAddr("no-such-master"), timeOut)
      assert(opt.isEmpty, s"unexpected: master with name '$masterName' was not supposed to be found")
    }
    "unknown master state" in {
      val opt = Await.result(sentinelClient.isMasterDown("no-such-master"), timeOut)
      assert(opt.isEmpty, "unexpected: master state should be unknown")
    }
    "master ok" in {
      assert(
        Await.result(sentinelClient.isMasterDown(masterName), timeOut) == Some(false),
        s"unexpected: master with name '$masterName' was not found"
      )
    }
    "slaves" in {
      val r = Await.result(sentinelClient.slaves(masterName), timeOut)
      assert(r.nonEmpty)
      assert(r(0)("flags").startsWith("slave"))
    }
    "reset bogus master" in {
      !Await.result(sentinelClient.resetMaster("no-such-master"), timeOut)
    }
    "reset master" in {
      Await.result(sentinelClient.resetMaster(masterName), timeOut)
    }
  }

}
