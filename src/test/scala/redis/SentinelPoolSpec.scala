package redis

import redis.RedisServerHelper.redisHost
import scala.concurrent.*
import scala.concurrent.duration.*

class SentinelMutablePoolSpec extends RedisSentinelClients("SentinelMutablePoolSpec") {

  var redisPool: RedisClientMutablePool = null

  override def setup(): Unit = {
    super.setup()
    redisPool = RedisClientMutablePool(Seq(RedisServer(redisHost, slavePort1)), masterName)
  }

  "mutable pool" should {
    "add remove" in {
      Thread.sleep(1000)
      assert(redisPool.redisConnectionPool.size == 1)

      redisPool.addServer(RedisServer(redisHost, slavePort2))
      redisPool.addServer(RedisServer(redisHost, slavePort2))
      Thread.sleep(5000)
      assert(redisPool.redisConnectionPool.size == 2)

      val key = "keyPoolDb0"
      val r = redisClient.set(key, "hello")

      Await.result(r, timeOut)
      within(500.millisecond) {
        assert(Await.result(redisPool.get[String](key), timeOut) == Some("hello"))
        assert(Await.result(redisPool.get[String](key), timeOut) == Some("hello"))
      }

      within(1.second) {
        redisPool.removeServer(RedisServer(redisHost, slavePort2))
      }

      awaitAssert(assert(redisPool.redisConnectionPool.size == 1), 5.second)

      assert(Await.result(redisPool.get[String](key), timeOut) == Some("hello"))
      assert(Await.result(redisPool.get[String](key), timeOut) == Some("hello"))

    }
  }

}

class SentinelMonitoredRedisClientMasterSlavesSpec extends RedisSentinelClients("SentinelMonitoredRedisClientMasterSlavesSpec") {

  lazy val redisMasterSlavesPool =
    SentinelMonitoredRedisClientMasterSlaves(master = masterName, sentinels = sentinelPorts.map((redisHost, _)))
  "sentinel slave pool" should {
    "add and remove" in {
      Thread.sleep(10000)
      Await.result(redisMasterSlavesPool.set("test", "value"), timeOut)
      awaitAssert(assert(redisMasterSlavesPool.slavesClients.redisConnectionPool.size == 2), 20.second)

      val newSlave = newSlaveProcess()

      awaitAssert(assert(redisMasterSlavesPool.slavesClients.redisConnectionPool.size == 3), 20.second)
      newSlave.stop()

      assert(Await.result(redisMasterSlavesPool.get[String]("test"), timeOut) == Some("value"))
      slave1.stop()
      slave2.stop()

      awaitAssert(assert(redisMasterSlavesPool.slavesClients.redisConnectionPool.size == 0), 20.second)
      assert(Await.result(redisMasterSlavesPool.get[String]("test"), timeOut) == Some("value"))
      newSlaveProcess()
      // println("************************** newSlaveProcess "+RedisServerHelper.portNumber.get())

      // within(30.second) {
      awaitAssert(assert(redisMasterSlavesPool.slavesClients.redisConnectionPool.size == 1), 20.second)
      assert(redisMasterSlavesPool.slavesClients.redisConnectionPool.size == 1)
      // }
    }
    /*
   "changemaster" in {
     Try(Await.result(redisMasterSlavesPool.masterClient.shutdown(), timeOut))
       awaitAssert( redisMasterSlavesPool.slavesClients.redisConnectionPool.size mustEqual 0, 20.second )
       Await.result(redisMasterSlavesPool.get[String]("test"), timeOut) mustEqual Some("value")
   }*/

  }
}
