package redis.commands

import redis._
import scala.concurrent.Await
import scala.concurrent.Future
import akka.util.ByteString
import redis.actors.ReplyErrorException

class StringsSpec extends RedisDockerServer {

  "Strings commands" should {
    "APPEND" in {
      val r = redis
        .set("appendKey", "Hello")
        .flatMap(_ => {
          redis
            .append("appendKey", " World")
            .flatMap(length => {
              assert(length == "Hello World".length)
              redis.get("appendKey")
            })
        })
      assert(Await.result(r, timeOut) == Some(ByteString("Hello World")))
    }

    "BITCOUNT" in {
      val r = redis
        .set("bitcountKey", "foobar")
        .flatMap(_ => {
          val a = redis.bitcount("bitcountKey")
          val b = redis.bitcount("bitcountKey", 0, 0)
          val c = redis.bitcount("bitcountKey", 1, 1)
          Future.sequence(Seq(a, b, c))
        })
      assert(Await.result(r, timeOut) == Seq(26, 4, 6))
    }

    "BITOP" should {
      val s1 = redis.set("bitopKey1", "afoobar a")
      val s2 = redis.set("bitopKey2", "aabcdef a")
      val r = for {
        _ <- s1
        _ <- s2
        and <- redis.bitopAND("ANDbitopKey", "bitopKey1", "bitopKey2")
        or <- redis.bitopOR("ORbitopKey", "bitopKey1", "bitopKey2")
        xor <- redis.bitopXOR("XORbitopKey", "bitopKey1", "bitopKey2")
        not <- redis.bitopNOT("NOTbitopKey", "bitopKey1")
      } yield {
        "AND" in {
          assert(Await.result(redis.get("ANDbitopKey"), timeOut) == Some(ByteString("a`bc`ab a")))
        }
        "OR" in {
          assert(Await.result(redis.get("ORbitopKey"), timeOut) == Some(ByteString("agoofev a")))
        }
        "XOR" in {
          assert(Await.result(redis.get("XORbitopKey"), timeOut) == Some(ByteString(0, 7, 13, 12, 6, 4, 20, 0, 0)))
        }
        "NOT" in {
          assert(Await.result(redis.get("NOTbitopKey"), timeOut) == Some(ByteString(-98, -103, -112, -112, -99, -98, -115, -33, -98)))
        }
      }
      Await.result(r, timeOut)
    }

    "BITPOS" in {
      val r = for {
        s1 <- redis.set("bitposKey", "a+b") // 01100001 00101011 01100010
        v1 <- redis.bitpos("bitposKey", 0)
        v2 <- redis.bitpos("bitposKey", 1)
        v3 <- redis.bitpos("bitposKey", 1, 1)
        v4 <- redis.bitpos("bitposKey", 0, 3)
        v5 <- redis.bitpos("bitposKey", 0, 1, 2)
      } yield {
        assert(s1)
        assert(v1 == 0)
        assert(v2 == 1)
        assert(v3 == 10)
        assert(v4 == -1)
        assert(v5 == 8)
      }
      Await.result(r, timeOut)
    }

    "DECR" in {
      val r = redis
        .set("decrKey", "10")
        .flatMap(_ => {
          redis.decr("decrKey")
        })
      val r2 = redis
        .set("decrKeyError", "234293482390480948029348230948")
        .flatMap(_ => {
          redis.decr("decrKeyError")
        })
      assert(Await.result(r, timeOut) == 9)
      assert(intercept[ReplyErrorException] { Await.result(r2, timeOut) }.getMessage == "ERR value is not an integer or out of range")
    }

    "DECRBY" in {
      val r = redis
        .set("decrbyKey", "10")
        .flatMap(_ => {
          redis.decrby("decrbyKey", 5)
        })
      assert(Await.result(r, timeOut) == 5)
    }

    "GET" in {
      val r = redis.get("getKeyNonexisting")
      val r2 = redis
        .set("getKey", "Hello")
        .flatMap(_ => {
          redis.get("getKey")
        })
      assert(Await.result(r, timeOut) == None)
      assert(Await.result(r2, timeOut) == Some(ByteString("Hello")))

      val rrr = for {
        r3 <- redis.get[String]("getKey")
      } yield {
        assert(r3 == Some("Hello"))
      }
      Await.result(rrr, timeOut)
    }

    "GET with conversion" in {
      val dumbObject = new DumbClass("foo", "bar")
      val r = redis
        .set("getDumbKey", dumbObject)
        .flatMap(_ => {
          redis.get[DumbClass]("getDumbKey")
        })
      assert(Await.result(r, timeOut) == Some(dumbObject))
    }

    "GETBIT" in {
      val r = redis.getbit("getbitKeyNonexisting", 0)
      val r2 = redis
        .set("getbitKey", "Hello")
        .flatMap(_ => {
          redis.getbit("getbitKey", 1)
        })
      assert(Await.result(r, timeOut) == false)
      assert(Await.result(r2, timeOut))
    }

    "GETRANGE" in {
      val r = redis
        .set("getrangeKey", "This is a string")
        .flatMap(_ => {
          Future.sequence(
            Seq(
              redis.getrange("getrangeKey", 0, 3),
              redis.getrange("getrangeKey", -3, -1),
              redis.getrange("getrangeKey", 0, -1),
              redis.getrange("getrangeKey", 10, 100)
            ).map(_.map(_.map(_.utf8String).get))
          )
        })
      assert(Await.result(r, timeOut) == Seq("This", "ing", "This is a string", "string"))
    }

    "GETSET" in {
      val r = redis
        .set("getsetKey", "Hello")
        .flatMap(_ => {
          redis
            .getset("getsetKey", "World")
            .flatMap(hello => {
              assert(hello == Some(ByteString("Hello")))
              redis.get("getsetKey")
            })
        })
      assert(Await.result(r, timeOut) == Some(ByteString("World")))
    }

    "INCR" in {
      val r = redis
        .set("incrKey", "10")
        .flatMap(_ => {
          redis.incr("incrKey")
        })
      assert(Await.result(r, timeOut) == 11)
    }

    "INCRBY" in {
      val r = redis
        .set("incrbyKey", "10")
        .flatMap(_ => {
          redis.incrby("incrbyKey", 5)
        })
      assert(Await.result(r, timeOut) == 15)
    }

    "INCRBYFLOAT" in {
      val r = redis
        .set("incrbyfloatKey", "10.50")
        .flatMap(_ => {
          redis.incrbyfloat("incrbyfloatKey", 0.15)
        })
      assert(Await.result(r, timeOut) == Some(10.65))
    }

    "MGET" in {
      val s1 = redis.set("mgetKey", "Hello")
      val s2 = redis.set("mgetKey2", "World")
      val r = for {
        _ <- s1
        _ <- s2
        mget <- redis.mget("mgetKey", "mgetKey2", "mgetKeyNonexisting")
      } yield {
        assert(mget == Seq(Some(ByteString("Hello")), Some(ByteString("World")), None))
      }
      Await.result(r, timeOut)
    }

    "MSET" in {
      val r = redis
        .mset(Map("msetKey" -> "Hello", "msetKey2" -> "World"))
        .flatMap(ok => {
          assert(ok)
          Future.sequence(
            Seq(
              redis.get("msetKey"),
              redis.get("msetKey2")
            )
          )
        })
      assert(Await.result(r, timeOut) == Seq(Some(ByteString("Hello")), Some(ByteString("World"))))
    }

    "MSETNX" in {
      val r = for {
        _ <- redis.del("msetnxKey", "msetnxKey2")
        msetnx <- redis.msetnx(Map("msetnxKey" -> "Hello", "msetnxKey2" -> "World"))
        msetnxFalse <- redis.msetnx(Map("msetnxKey3" -> "Hello", "msetnxKey2" -> "already set !!"))
      } yield {
        assert(msetnx)
        assert(msetnxFalse == false)
      }
      Await.result(r, timeOut)
    }

    "PSETEX" in {
      val r = redis
        .psetex("psetexKey", 2000, "temp value")
        .flatMap(x => {
          assert(x)
          redis
            .get("psetexKey")
            .flatMap(v => {
              assert(v == Some(ByteString("temp value")))
              Thread.sleep(2000)
              redis.get("psetexKey")
            })
        })
      assert(Await.result(r, timeOut) == None)
    }

    "SET" in {
      val rr = for {
        r <- redis.set("setKey", "value")
        ex <- redis.set("setKey", "value", exSeconds = Some(2))
        nxex <- redis.set("setKey2", "value", NX = true, exSeconds = Some(60))
        ttlnxex <- redis.ttl("setKey2")
        xxex <- redis.set("setKey2", "value", XX = true, exSeconds = Some(180))
        ttlxxex <- redis.ttl("setKey2")
        _ <- redis.del("setKey2")
        px <- redis.set("setKey", "value", pxMilliseconds = Some(1))
        nxTrue <- {
          Thread.sleep(20)
          redis.set("setKey", "value", NX = true)
        }
        xx <- redis.set("setKey", "value", XX = true)
        nxFalse <- redis.set("setKey", "value", NX = true)
      } yield {
        assert(r)
        assert(ex)
        assert(nxex)
        assert((0 <= ttlnxex) && (ttlnxex <= 60))
        assert(xxex)
        assert((60 <= ttlxxex) && (ttlxxex <= 180))
        assert(px)
        assert(nxTrue) // because pxMilliseconds = 1 millisecond
        assert(xx)
        assert(nxFalse == false)
      }
      Await.result(rr, timeOut)
    }

    "SETBIT" in {
      val r = for {
        _ <- redis.del("setbitKey")
        setTrue <- redis.setbit("setbitKey", 1, value = true)
        getTrue <- redis.getbit("setbitKey", 1)
        setFalse <- redis.setbit("setbitKey", 1, value = false)
        getFalse <- redis.getbit("setbitKey", 1)
      } yield {
        assert(setTrue == false)
        assert(getTrue)
        assert(setFalse)
        assert(getFalse == false)
      }
      Await.result(r, timeOut)
    }

    "SETEX" in {
      val r = redis
        .setex("setexKey", 1, "temp value")
        .flatMap(x => {
          assert(x)
          redis
            .get("setexKey")
            .flatMap(v => {
              assert(v == Some(ByteString("temp value")))
              Thread.sleep(2000)
              redis.get("setexKey")
            })
        })
      assert(Await.result(r, timeOut) == None)
    }

    "SETNX" in {
      val r = for {
        _ <- redis.del("setnxKey")
        s1 <- redis.setnx("setnxKey", "Hello")
        s2 <- redis.setnx("setnxKey", "World")
      } yield {
        assert(s1)
        assert(s2 == false)
      }
      Await.result(r, timeOut)
    }

    "SETRANGE" in {
      val r = redis
        .set("setrangeKey", "Hello World")
        .flatMap(d => {
          redis
            .setrange("setrangeKey", 6, "Redis")
            .flatMap(length => {
              assert(length == "Hello Redis".length)
              redis.get("setrangeKey")
            })
        })
      assert(Await.result(r, timeOut) == Some(ByteString("Hello Redis")))
    }

    "STRLEN" in {
      val r = redis
        .set("strlenKey", "Hello World")
        .flatMap(d => {
          redis
            .strlen("strlenKey")
            .flatMap(length => {
              assert(length == "Hello World".length)
              redis.strlen("strlenKeyNonexisting")
            })
        })
      assert(Await.result(r, timeOut) == 0)
    }
  }
}
