package redis.commands

import redis.RedisDockerServer
import scala.concurrent.Await

class HyperLogLogSpec extends RedisDockerServer {

  "HyperLogLog commands" should {
    "PFADD" in {
      val r = redis
        .pfadd("hll", "a", "b", "c", "d", "e", "f", "g")
        .flatMap(_ => {
          redis
            .pfcount("hll")
            .flatMap(count => {
              assert(count == 7)
              redis
                .pfadd("hll", "h", "i")
                .flatMap(_ => {
                  redis.pfcount("hll")
                })
            })
        })
      assert(Await.result(r, timeOut) == 9)
    }

    "PFCOUNT" in {
      val r = redis
        .pfadd("hll2", "a", "b", "c", "d", "e", "f", "g")
        .flatMap(_ => {
          redis.pfcount("hll2")
        })
      assert(Await.result(r, timeOut) == 7)
    }

    "PFMERGE" in {
      val r = redis
        .pfadd("hll3", "a", "b")
        .flatMap(_ => {
          redis
            .pfadd("hll4", "c", "d")
            .flatMap(_ => {
              redis
                .pfmerge("hll5", "hll4", "hll3")
                .flatMap(merged => {
                  assert(merged)
                  redis.pfcount("hll5")
                })
            })
        })

      assert(Await.result(r, timeOut) == 4)
    }
  }
}
