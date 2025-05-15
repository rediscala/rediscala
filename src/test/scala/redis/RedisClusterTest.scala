package redis

import java.util.Base64
import org.apache.pekko.util.ByteString
import redis.api.clusters.ClusterSlots
import redis.protocol.*
import scala.concurrent.Await

/**
  * Created by npeters on 20/05/16.
  */
class RedisClusterTest extends RedisClusterClients {

  var redisCluster: RedisCluster = null
  override def setup(): Unit = {
    super.setup()
    redisCluster = RedisCluster(nodePorts.map(p => RedisServer("127.0.0.1", p)))
  }

  "RedisComputeSlot" should {
    "simple" in {
      assert(RedisComputeSlot.hashSlot("foo") == 12182)
      assert(RedisComputeSlot.hashSlot("somekey") == 11058)
      assert(RedisComputeSlot.hashSlot("somekey3452345325453532452345") == 15278)
      assert(RedisComputeSlot.hashSlot("rzarzaZERAZERfqsfsdQSFD") == 14258)
      assert(RedisComputeSlot.hashSlot("{foo}46546546546") == 12182)
      assert(RedisComputeSlot.hashSlot("foo_312312") == 5839)
      assert(RedisComputeSlot.hashSlot("aazaza{aa") == 11473)
    }
  }

  "clusterSlots" should {
    "encoding" in {
      val clusterSlotsAsByteString = ByteString(
        Base64.getDecoder.decode(
          "KjMNCio0DQo6MA0KOjU0NjANCiozDQokOQ0KMTI3LjAuMC4xDQo6NzAwMA0KJDQwDQplNDM1OTlkZmY2ZTNhN2I5ZWQ1M2IxY2EwZGI0YmQwMDlhODUwYmE1DQoqMw0KJDkNCjEyNy4wLjAuMQ0KOjcwMDMNCiQ0MA0KYzBmNmYzOWI2NDg4MTVhMTllNDlkYzQ1MzZkMmExM2IxNDdhOWY1MA0KKjQNCjoxMDkyMw0KOjE2MzgzDQoqMw0KJDkNCjEyNy4wLjAuMQ0KOjcwMDINCiQ0MA0KNDhkMzcxMjBmMjEzNTc4Y2IxZWFjMzhlNWYyYmY1ODlkY2RhNGEwYg0KKjMNCiQ5DQoxMjcuMC4wLjENCjo3MDA1DQokNDANCjE0Zjc2OWVlNmU1YWY2MmZiMTc5NjZlZDRlZWRmMTIxOWNjYjE1OTINCio0DQo6NTQ2MQ0KOjEwOTIyDQoqMw0KJDkNCjEyNy4wLjAuMQ0KOjcwMDENCiQ0MA0KYzhlYzM5MmMyMjY5NGQ1ODlhNjRhMjA5OTliNGRkNWNiNDBlNDIwMQ0KKjMNCiQ5DQoxMjcuMC4wLjENCjo3MDA0DQokNDANCmVmYThmZDc0MDQxYTNhOGQ3YWYyNWY3MDkwM2I5ZTFmNGMwNjRhMjENCg=="
        )
      )
      val clusterSlotsAsBulk: DecodeResult[RedisReply] = RedisProtocolReply.decodeReply(clusterSlotsAsByteString)
      val dr: DecodeResult[String] = clusterSlotsAsBulk.map {
        case a: MultiBulk =>
          ClusterSlots().decodeReply(a).map(c => c.copy(slaves = c.slaves.toList)).toString()
        case _ => "fail"
      }

      dr match {
        case FullyDecoded(decodeValue, _) =>
          assert(
            decodeValue == "Vector(ClusterSlot(0,5460,ClusterNode(127.0.0.1,7000,e43599dff6e3a7b9ed53b1ca0db4bd009a850ba5),List(ClusterNode(127.0.0.1,7003,c0f6f39b648815a19e49dc4536d2a13b147a9f50))), " +
              "ClusterSlot(10923,16383,ClusterNode(127.0.0.1,7002,48d37120f213578cb1eac38e5f2bf589dcda4a0b),List(ClusterNode(127.0.0.1,7005,14f769ee6e5af62fb17966ed4eedf1219ccb1592))), " +
              "ClusterSlot(5461,10922,ClusterNode(127.0.0.1,7001,c8ec392c22694d589a64a20999b4dd5cb40e4201),List(ClusterNode(127.0.0.1,7004,efa8fd74041a3a8d7af25f70903b9e1f4c064a21))))"
          )

        case x => fail(s"unexpected ${x}")
      }
    }

  }

  "Strings" should {
    "set-get" in {
      println("set")
      Await.result(redisCluster.set[String]("foo", "FOO"), timeOut)
      println("exists")
      assert(Await.result(redisCluster.exists("foo"), timeOut))

      println("get")
      assert(Await.result(redisCluster.get[String]("foo"), timeOut) == Some("FOO"))

      println("del")
      Await.result(redisCluster.del("foo", "foo"), timeOut)

      println("exists")
      assert(Await.result(redisCluster.exists("foo"), timeOut) == false)

    }

    "mset-mget" in {
      println("mset")
      Await.result(redisCluster.mset[String](Map("{foo}1" -> "FOO1", "{foo}2" -> "FOO2")), timeOut)
      println("exists")
      assert(Await.result(redisCluster.exists("{foo}1"), timeOut))
      assert(Await.result(redisCluster.exists("{foo}2"), timeOut))

      println("mget")
      assert(Await.result(redisCluster.mget[String]("{foo}1", "{foo}2"), timeOut) == Seq(Some("FOO1"), Some("FOO2")))

      println("del")
      Await.result(redisCluster.del("{foo}1", "{foo}2"), timeOut)

      println("exists")
      assert(Await.result(redisCluster.exists("{foo}1"), timeOut) == false)

    }
  }

  "tools" should {
    "groupby" in {
      assert(
        redisCluster.groupByClusterServer(Seq("{foo1}1", "{foo2}1", "{foo1}2", "{foo2}2")).sortBy(_.head).toList == Seq(
          Seq("{foo2}1", "{foo2}2"),
          Seq("{foo1}1", "{foo1}2")
        ).sortBy(_.head)
      )
    }
  }

  "long run" should {
    "wait" in {
      println("set " + redisCluster.getClusterAndConnection(RedisComputeSlot.hashSlot("foo1")).get._1.master.toString)
      Await.result(redisCluster.set[String]("foo1", "FOO"), timeOut)
      Await.result(redisCluster.get[String]("foo1"), timeOut)
      println("wait...")
      // Thread.sleep(15000)
      println("get")
      assert(Await.result(redisCluster.get[String]("foo1"), timeOut) == Some("FOO"))

    }

  }

  "clusterInfo" should {
    "just work" in {
      val res = Await.result(redisCluster.clusterInfo(), timeOut)
      assert(res.nonEmpty)
      for (v <- res) {
        println(s"Key  ${v._1} value ${v._2}")
      }
      assert(res("cluster_state") == "ok")
      assert(res("cluster_slots_ok") == "16384")
      assert(res("cluster_known_nodes") == "6")
      assert(res("cluster_size") == "3")
    }
  }

  "clusterNodes" should {
    "just work" in {
      val res = Await.result(redisCluster.clusterNodes(), timeOut)
      assert(res.nonEmpty)
      for (m <- res) {
        println(m.toString)
      }
      assert(res.length == 6)
      assert(res.count(_.master != "-") == 3)
      assert(res.count(_.link_state == "connected") == 6)
    }
  }
}
