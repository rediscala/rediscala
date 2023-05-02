package redis.commands

import redis._
import scala.concurrent.Await
import akka.util.ByteString

class SetsSpec extends RedisDockerServer {

  "Sets commands" should {
    "SADD" in {
      val r = for {
        _ <- redis.del("saddKey")
        s1 <- redis.sadd("saddKey", "Hello", "World")
        s2 <- redis.sadd("saddKey", "World")
        m <- redis.smembers("saddKey")
      } yield {
        assert(s1 == 2)
        assert(s2 == 0)
        assert(m.toSet == Seq(ByteString("Hello"), ByteString("World")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SCARD" in {
      val r = for {
        _ <- redis.del("scardKey")
        c1 <- redis.scard("scardKey")
        _ <- redis.sadd("scardKey", "Hello", "World")
        c2 <- redis.scard("scardKey")
      } yield {
        assert(c1 == 0)
        assert(c2 == 2)
      }
      Await.result(r, timeOut)
    }

    "SDIFF" in {
      val r = for {
        _ <- redis.del("sdiffKey1")
        _ <- redis.del("sdiffKey2")
        _ <- redis.sadd("sdiffKey1", "a", "b", "c")
        _ <- redis.sadd("sdiffKey2", "c", "d", "e")
        diff <- redis.sdiff("sdiffKey1", "sdiffKey2")
      } yield {
        assert(diff.toSet == Seq(ByteString("a"), ByteString("b")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SDIFFSTORE" in {
      val r = for {
        _ <- redis.del("sdiffstoreKey1")
        _ <- redis.del("sdiffstoreKey2")
        _ <- redis.sadd("sdiffstoreKey1", "a", "b", "c")
        _ <- redis.sadd("sdiffstoreKey2", "c", "d", "e")
        diff <- redis.sdiffstore("sdiffstoreKeyDest", "sdiffstoreKey1", "sdiffstoreKey2")
        m <- redis.smembers("sdiffstoreKeyDest")
      } yield {
        assert(diff == 2)
        assert(m.toSet == Seq(ByteString("a"), ByteString("b")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SINTER" in {
      val r = for {
        _ <- redis.del("sinterKey1")
        _ <- redis.del("sinterKey2")
        _ <- redis.sadd("sinterKey1", "a", "b", "c")
        _ <- redis.sadd("sinterKey2", "c", "d", "e")
        inter <- redis.sinter("sinterKey1", "sinterKey2")
      } yield {
        assert(inter.toSet == Seq(ByteString("c")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SINTERSTORE" in {
      val r = for {
        _ <- redis.del("sinterstoreKey1")
        _ <- redis.del("sinterstoreKey2")
        _ <- redis.sadd("sinterstoreKey1", "a", "b", "c")
        _ <- redis.sadd("sinterstoreKey2", "c", "d", "e")
        inter <- redis.sinterstore("sinterstoreKeyDest", "sinterstoreKey1", "sinterstoreKey2")
        m <- redis.smembers("sinterstoreKeyDest")
      } yield {
        assert(inter == 1)
        assert(m.toSet == Seq(ByteString("c")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SISMEMBER" in {
      val r = for {
        _ <- redis.del("sismemberKey")
        _ <- redis.sadd("sismemberKey", "Hello", "World")
        is <- redis.sismember("sismemberKey", "World")
        isNot <- redis.sismember("sismemberKey", "not member")
      } yield {
        assert(is)
        assert(isNot == false)
      }
      Await.result(r, timeOut)
    }

    "SMEMBERS" in {
      val r = for {
        _ <- redis.del("smembersKey")
        _ <- redis.sadd("smembersKey", "Hello", "World")
        m <- redis.smembers("smembersKey")
      } yield {
        assert(m.toSet == Seq(ByteString("Hello"), ByteString("World")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SMOVE" in {
      val r = for {
        _ <- redis.del("smoveKey1")
        _ <- redis.del("smoveKey2")
        _ <- redis.sadd("smoveKey1", "one", "two")
        _ <- redis.sadd("smoveKey2", "three")
        isMoved <- redis.smove("smoveKey1", "smoveKey2", "two")
        isNotMoved <- redis.smove("smoveKey1", "smoveKey2", "non existing")
        m <- redis.smembers("smoveKey2")
      } yield {
        assert(isMoved)
        assert(isNotMoved == false)
        assert(m.toSet == Seq(ByteString("three"), ByteString("two")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SPOP" in {
      val r = for {
        _ <- redis.del("spopKey")
        _ <- redis.sadd("spopKey", "one", "two", "three")
        pop <- redis.spop("spopKey")
        popNone <- redis.spop("spopKeyNonExisting")
        m <- redis.smembers("spopKey")
      } yield {
        assert(Seq(ByteString("three"), ByteString("two"), ByteString("one")).contains(pop.get))
        assert(popNone.isEmpty)
        assert(m.forall(Set(ByteString("three"), ByteString("two"), ByteString("one"))))
      }
      Await.result(r, timeOut)
    }

    "SRANDMEMBER" in {
      val r = for {
        _ <- redis.del("srandmemberKey")
        _ <- redis.sadd("srandmemberKey", "one", "two", "three")
        randmember <- redis.srandmember("srandmemberKey")
        randmember2 <- redis.srandmember("srandmemberKey", 2)
        randmemberNonExisting <- redis.srandmember("srandmemberKeyNonExisting", 2)
        m <- redis.smembers("spopKey")
      } yield {
        assert(Seq(ByteString("three"), ByteString("two"), ByteString("one")).contains(randmember.get))
        assert(randmember2.size == 2)
        assert(randmemberNonExisting.isEmpty)
      }
      Await.result(r, timeOut)
    }

    "SREM" in {
      val r = for {
        _ <- redis.del("sremKey")
        _ <- redis.sadd("sremKey", "one", "two", "three", "four")
        rem <- redis.srem("sremKey", "one", "four")
        remNothing <- redis.srem("sremKey", "five")
        m <- redis.smembers("sremKey")
      } yield {
        assert(rem == 2)
        assert(remNothing == 0)
        assert(m.toSet == Seq(ByteString("three"), ByteString("two")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SSCAN" in {
      val r = for {
        _ <- redis.sadd("sscan", (1 to 20).map(_.toString): _*)
        scanResult <- redis.sscan[String]("sscan", count = Some(100))
      } yield {
        assert(scanResult.index == 0)
        assert(scanResult.data.map(_.toInt).sorted == (1 to 20))
      }

      Await.result(r, timeOut)
    }

    "SUNION" in {
      val r = for {
        _ <- redis.del("sunionKey1")
        _ <- redis.del("sunionKey2")
        _ <- redis.sadd("sunionKey1", "a", "b", "c")
        _ <- redis.sadd("sunionKey2", "c", "d", "e")
        union <- redis.sunion("sunionKey1", "sunionKey2")
      } yield {
        assert(union.toSet == Seq(ByteString("a"), ByteString("b"), ByteString("c"), ByteString("d"), ByteString("e")).toSet)
      }
      Await.result(r, timeOut)
    }

    "SUNIONSTORE" in {
      val r = for {
        _ <- redis.del("sunionstoreKey1")
        _ <- redis.del("sunionstoreKey2")
        _ <- redis.sadd("sunionstoreKey1", "a", "b", "c")
        _ <- redis.sadd("sunionstoreKey2", "c", "d", "e")
        union <- redis.sunionstore("sunionstoreKeyDest", "sunionstoreKey1", "sunionstoreKey2")
        m <- redis.smembers("sunionstoreKeyDest")
      } yield {
        assert(union == 5)
        assert(m.toSet == Seq(ByteString("a"), ByteString("b"), ByteString("c"), ByteString("d"), ByteString("e")).toSet)
      }
      Await.result(r, timeOut)
    }
  }
}
