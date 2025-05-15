package redis.commands

import org.apache.pekko.util.ByteString
import redis.*
import redis.actors.ReplyErrorException
import scala.concurrent.Await
import scala.util.Success

class HashesSpec extends RedisDockerServer {

  "Hashes commands" should {
    "HDEL" in {
      val r = for {
        _ <- redis.hset("hdelKey", "field", "value")
        d <- redis.hdel("hdelKey", "field", "fieldNonexisting")
      } yield {
        assert(d == 1)
      }
      Await.result(r, timeOut)
    }

    "HEXISTS" in {
      val r = for {
        _ <- redis.hset("hexistsKey", "field", "value")
        exist <- redis.hexists("hexistsKey", "field")
        notExist <- redis.hexists("hexistsKey", "fieldNotExisting")
      } yield {
        assert(exist)
        assert(notExist == false)
      }
      Await.result(r, timeOut)
    }

    "HGET" in {
      val r = for {
        _ <- redis.hset("hgetKey", "field", "value")
        get <- redis.hget("hgetKey", "field")
        get2 <- redis.hget("hgetKey", "fieldNotExisting")
      } yield {
        assert(get == Some(ByteString("value")))
        assert(get2 == None)
      }
      Await.result(r, timeOut)
    }

    "HGETALL" in {
      val r = for {
        _ <- redis.hset("hgetallKey", "field", "value")
        get <- redis.hgetall("hgetallKey")
        get2 <- redis.hgetall("hgetallKeyNotExisting")
      } yield {
        assert(get == Map("field" -> ByteString("value")))
        assert(get2 == Map.empty)
      }
      Await.result(r, timeOut)
    }

    "HINCRBY" in {
      val r = for {
        _ <- redis.hset("hincrbyKey", "field", "10")
        i <- redis.hincrby("hincrbyKey", "field", 1)
        ii <- redis.hincrby("hincrbyKey", "field", -1)
      } yield {
        assert(i == 11)
        assert(ii == 10)
      }
      Await.result(r, timeOut)
    }

    "HINCRBYFLOAT" in {
      val r = for {
        _ <- redis.hset("hincrbyfloatKey", "field", "10.5")
        i <- redis.hincrbyfloat("hincrbyfloatKey", "field", 0.1)
        ii <- redis.hincrbyfloat("hincrbyfloatKey", "field", -1.1)
      } yield {
        assert(i == 10.6)
        assert(ii == 9.5)
      }
      Await.result(r, timeOut)
    }

    "HKEYS" in {
      val r = for {
        _ <- redis.hset("hkeysKey", "field", "value")
        keys <- redis.hkeys("hkeysKey")
      } yield {
        assert(keys == Seq("field"))
      }
      Await.result(r, timeOut)
    }

    "HLEN" in {
      val r = for {
        _ <- redis.hset("hlenKey", "field", "value")
        hLength <- redis.hlen("hlenKey")
      } yield {
        assert(hLength == 1)
      }
      Await.result(r, timeOut)
    }

    "HMGET" in {
      val r = for {
        _ <- redis.hset("hmgetKey", "field", "value")
        hmget <- redis.hmget("hmgetKey", "field", "nofield")
      } yield {
        assert(hmget == Seq(Some(ByteString("value")), None))
      }
      Await.result(r, timeOut)
    }

    "HMSET" in {
      val r = for {
        _ <- redis.hmset("hmsetKey", Map("field" -> "value1", "field2" -> "value2"))
        v1 <- redis.hget("hmsetKey", "field")
        v2 <- redis.hget("hmsetKey", "field2")
      } yield {
        assert(v1 == Some(ByteString("value1")))
        assert(v2 == Some(ByteString("value2")))
      }
      Await.result(r, timeOut)
    }

    "HSET" in {
      val r = for {
        _ <- redis.hdel("hsetKey", "field")
        set <- redis.hset("hsetKey", "field", "value")
        update <- redis.hset("hsetKey", "field", "value2")
        v1 <- redis.hget("hsetKey", "field")
        empty <- redis.hset("hsetKey", Map.empty[String, String]).transform(Success.apply)
        v2 <- redis.hget("hsetKey", "field")
        values = (1 to 5).map(_.toString).map(n => (n, n)).toMap + ("field" -> "aaa")
        update2 <- redis.hset("hsetKey", values)
        v3 <- redis.hgetall[String]("hsetKey")
      } yield {
        assert(set)
        assert(update == false)
        assert(v1 == Some(ByteString("value2")))
        assert(empty.toEither.left.exists(_.isInstanceOf[ReplyErrorException]))
        assert(v2 == Some(ByteString("value2")))
        assert(update2 == 5L)
        assert(v3 == values)
      }
      Await.result(r, timeOut)
    }

    "HMSETNX" in {
      val r = for {
        _ <- redis.hdel("hsetnxKey", "field")
        set <- redis.hsetnx("hsetnxKey", "field", "value")
        doNothing <- redis.hsetnx("hsetnxKey", "field", "value2")
        v1 <- redis.hget("hsetnxKey", "field")
      } yield {
        assert(set)
        assert(doNothing == false)
        assert(v1 == Some(ByteString("value")))
      }
      Await.result(r, timeOut)
    }

    "HSCAN" in {
      val initialData = (1 to 20).grouped(2).map(x => x.head.toString -> x.tail.head.toString).toMap
      val r = for {
        _ <- redis.del("hscan")
        _ <- redis.hmset("hscan", initialData)
        scanResult <- redis.hscan[String]("hscan", count = Some(300))
      } yield {
        assert(scanResult.data.values.toList.map(_.toInt).sorted == (2 to 20 by 2))
        assert(scanResult.index == 0)
      }
      Await.result(r, timeOut)
    }

    "HVALS" in {
      val r = for {
        _ <- redis.hdel("hvalsKey", "field")
        empty <- redis.hvals("hvalsKey")
        _ <- redis.hset("hvalsKey", "field", "value")
        some <- redis.hvals("hvalsKey")
      } yield {
        assert(empty.isEmpty)
        assert(some == Seq(ByteString("value")))
      }
      Await.result(r, timeOut)
    }
  }
}
