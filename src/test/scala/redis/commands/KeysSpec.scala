package redis.commands

import org.apache.pekko.util.ByteString
import redis.*
import redis.api.*
import scala.concurrent.Await
import scala.concurrent.Future

class KeysSpec extends RedisStandaloneServer {

  "Keys commands" should {
    "DEL" in {
      val r = for {
        s <- redis.set("delKey", "value")
        d <- redis.del("delKey", "delKeyNonexisting")
      } yield {
        assert(s)
        assert(d == 1)
      }
      Await.result(r, timeOut)
    }
    "DUMP" in {
      val r = for {
        s <- redis.set("dumpKey", "value")
        d <- redis.dump("dumpKey")
      } yield {
        assert(s)
        assert(d == Some(ByteString(0, 5, 118, 97, 108, 117, 101, 9, 0, 81, 4, -112, -12, -107, 44, -8, -33)))
      }
      Await.result(r, timeOut)
    }

    "EXISTS" in {
      val r = for {
        s <- redis.set("existsKey", "value")
        e <- redis.exists("existsKey")
        e2 <- redis.exists("existsKeyNonexisting")
      } yield {
        assert(s)
        assert(e)
        assert(e2 == false)
      }
      Await.result(r, timeOut)
    }

    "EXISTS variadic" in {
      val r = for {
        s <- redis.set("existsKey", "value")
        e <- redis.existsMany("existsKey", "existsKeyNonexisting")
        e2 <- redis.existsMany("existsKeyNonexisting")
      } yield {
        assert(s)
        assert(e == 1)
        assert(e2 == 0)
      }
      Await.result(r, timeOut)
    }

    "EXPIRE" in {
      val r = for {
        s <- redis.set("expireKey", "value")
        e <- redis.expire("expireKey", 1)
        e2 <- redis.expire("expireKeyNonexisting", 1)
        expired <- {
          Thread.sleep(1000)
          redis.get("expireKey")
        }
      } yield {
        assert(s)
        assert(e)
        assert(e2 == false)
        assert(expired == None)
      }
      Await.result(r, timeOut)
    }

    "EXPIREAT" in {
      val r = for {
        s <- redis.set("expireatKey", "value")
        e <- redis.expireat("expireatKey", System.currentTimeMillis() / 1000)
        expired <- redis.get("expireatKey")
      } yield {
        assert(s)
        assert(e)
        assert(expired == None)
      }
      Await.result(r, timeOut)
    }

    "KEYS" in {
      val r = for {
        _ <- redis.set("keysKey", "value")
        _ <- redis.set("keysKey2", "value")
        k <- redis.keys("keysKey*")
        k2 <- redis.keys("keysKey?")
        k3 <- redis.keys("keysKeyNomatch")
      } yield {
        assert(k.toSet == Set("keysKey2", "keysKey"))
        assert(k2.toSet == Set("keysKey2"))
        assert(k3.isEmpty)
      }
      Await.result(r, timeOut)
    }

    "MIGRATE" in {
      import scala.concurrent.duration.*

      withRedisServer(port => {
        val redisMigrate = RedisClient("localhost", port)
        val key = "migrateKey-" + System.currentTimeMillis()
        val r = for {
          _ <- redis.set(key, "value")
          m <- redis.migrate("localhost", port, key, 0, 10.seconds)
          getSource <- redis.get(key)
          getTarget <- redisMigrate.get(key)
        } yield {
          assert(m)
          assert(getSource == None)
          assert(getTarget == Some(ByteString("value")))
        }
        Await.result(r, timeOut)
      })
    }

    "MIGRATE COPY" in {
      import scala.concurrent.duration.*

      withRedisServer(port => {
        val redisMigrate = RedisClient("localhost", port)
        val key = "migrateCopyKey-" + System.currentTimeMillis()
        val r = for {
          _ <- redis.set(key, "value")
          m <- redis.migrate("localhost", port, key, 0, 10.seconds, copy = true)
          getSource <- redis.get(key)
          getTarget <- redisMigrate.get(key)
        } yield {
          assert(m)
          assert(getSource == Some(ByteString("value")))
          assert(getTarget == Some(ByteString("value")))
        }
        Await.result(r, timeOut)
      })
    }

    "MIGRATE KEYS" in {
      import scala.concurrent.duration.*

      withRedisServer(port => {
        val redisMigrate = RedisClient("localhost", port)
        val key1 = "migrateKey1-" + System.currentTimeMillis()
        val key2 = "migrateKey2" + System.currentTimeMillis()
        val r = for {
          _ <- redis.set(key1, "value")
          _ <- redis.set(key2, "value")
          m <- redis.migrateMany("localhost", port, Seq(key1, key2), 0, 10.seconds)
          get1 <- redisMigrate.get(key1)
          get2 <- redisMigrate.get(key2)
        } yield {
          assert(m)
          assert(get1 == Some(ByteString("value")))
          assert(get2 == Some(ByteString("value")))
        }
        Await.result(r, timeOut)
      })
    }

    "MIGRATE REPLACE" in {
      import scala.concurrent.duration.*

      withRedisServer(port => {
        val redisMigrate = RedisClient("localhost", port)
        val key = "migrateReplaceKey-" + System.currentTimeMillis()
        val r = for {
          _ <- redis.set(key, "value1")
          _ <- redisMigrate.set(key, "value2")
          m2 <- redis.migrate("localhost", port, key, 0, 10.seconds, replace = true)
          get <- redisMigrate.get(key)
        } yield {
          assert(m2)
          assert(get == Some(ByteString("value1")))
        }
        Await.result(r, timeOut)
      })
    }

    "MOVE" in {
      val redisMove = RedisClient(port = port)
      val r = for {
        _ <- redis.set("moveKey", "value")
        _ <- redisMove.select(1)
        _ <- redisMove.del("moveKey")
        move <- redis.move("moveKey", 1)
        move2 <- redis.move("moveKey2", 1)
        get <- redisMove.get("moveKey")
        get2 <- redisMove.get("moveKey2")
      } yield {
        assert(move)
        assert(move2 == false)
        assert(get == Some(ByteString("value")))
        assert(get2 == None)
      }
      Await.result(r, timeOut)
    }

    "OBJECT" should {
      "REFCOUNT" in {
        val r = for {
          _ <- redis.set("objectRefcount", "objectRefcountValue")
          ref <- redis.objectRefcount("objectRefcount")
          refNotFound <- redis.objectRefcount("objectRefcountNotFound")
        } yield {
          assert(ref == Some(1))
          assert(refNotFound.isEmpty)
        }
        Await.result(r, timeOut)
      }
      "IDLETIME" in {
        val r = for {
          _ <- redis.set("objectIdletime", "objectRefcountValue")
          time <- redis.objectIdletime("objectIdletime")
          timeNotFound <- redis.objectIdletime("objectIdletimeNotFound")
        } yield {
          assert(time.isDefined)
          assert(timeNotFound.isEmpty)
        }
        Await.result(r, timeOut)
      }
      "ENCODING" in {
        val r = for {
          _ <- redis.set("objectEncoding", "objectRefcountValue")
          encoding <- redis.objectEncoding("objectEncoding")
          encodingNotFound <- redis.objectEncoding("objectEncodingNotFound")
        } yield {
          assert(encoding.isDefined)
          assert(encodingNotFound.isEmpty)
        }
        Await.result(r, timeOut)
      }
    }

    "PERSIST" in {
      val r = for {
        s <- redis.set("persistKey", "value")
        e <- redis.expire("persistKey", 10)
        ttl <- redis.ttl("persistKey")
        p <- redis.persist("persistKey")
        ttl2 <- redis.ttl("persistKey")
      } yield {
        assert(s)
        assert(e)
        assert((1 <= ttl.toInt) && (ttl.toInt <= 10))
        assert(p)
        assert(ttl2 == -1)
      }
      Await.result(r, timeOut)
    }

    "PEXPIRE" in {
      val r = for {
        s <- redis.set("pexpireKey", "value")
        e <- redis.pexpire("pexpireKey", 1500)
        e2 <- redis.expire("pexpireKeyNonexisting", 1500)
        expired <- {
          Thread.sleep(1500)
          redis.get("pexpireKey")
        }
      } yield {
        assert(s)
        assert(e)
        assert(e2 == false)
        assert(expired == None)
      }
      Await.result(r, timeOut)
    }

    "PEXPIREAT" in {
      val r = for {
        s <- redis.set("pexpireatKey", "value")
        e <- redis.pexpireat("pexpireatKey", System.currentTimeMillis())
        expired <- redis.get("pexpireatKey")
      } yield {
        assert(s)
        assert(e)
        assert(expired == None)
      }
      Await.result(r, timeOut)
    }

    "PTTL" in {
      val r = for {
        s <- redis.set("pttlKey", "value")
        e <- redis.expire("pttlKey", 1)
        pttl <- redis.pttl("pttlKey")
      } yield {
        assert(s)
        assert(e)
        assert((1 <= pttl) && (pttl <= 1000))
      }
      Await.result(r, timeOut)
    }

    "RANDOMKEY" in {
      val r = for {
        s <- redis.set("randomKey", "value") // could fail if database was empty
        s <- redis.randomkey()
      } yield {
        assert(s.isDefined)
      }
      Await.result(r, timeOut)
    }

    "RENAME" in {
      val r = for {
        _ <- redis.del("renameNewKey")
        s <- redis.set("renameKey", "value")
        rename <- redis.rename("renameKey", "renameNewKey")
        renamedValue <- redis.get("renameNewKey")
      } yield {
        assert(s)
        assert(rename)
        assert(renamedValue == Some(ByteString("value")))
      }
      Await.result(r, timeOut)
    }

    "RENAMENX" in {
      val r = for {
        _ <- redis.del("renamenxNewKey")
        s <- redis.set("renamenxKey", "value")
        s <- redis.set("renamenxNewKey", "value")
        rename <- redis.renamenx("renamenxKey", "renamenxNewKey")
        _ <- redis.del("renamenxNewKey")
        rename2 <- redis.renamenx("renamenxKey", "renamenxNewKey")
        renamedValue <- redis.get("renamenxNewKey")
      } yield {
        assert(s)
        assert(rename == false)
        assert(rename2)
        assert(renamedValue == Some(ByteString("value")))
      }
      Await.result(r, timeOut)
    }

    "RESTORE" in {
      val r = for {
        s <- redis.set("restoreKey", "value")
        dump <- redis.dump("restoreKey")
        _ <- redis.del("restoreKey")
        restore <- redis.restore("restoreKey", serializedValue = dump.get)
      } yield {
        assert(s)
        assert(dump == Some(ByteString(0, 5, 118, 97, 108, 117, 101, 9, 0, 81, 4, -112, -12, -107, 44, -8, -33)))
        assert(restore)
      }
      Await.result(r, timeOut)
    }

    "SCAN" in {

      withRedisServer(port => {
        val scanRedis = RedisClient("localhost", port)

        val r = for {
          _ <- scanRedis.flushdb()
          _ <- scanRedis.set("scanKey1", "value1")
          _ <- scanRedis.set("scanKey2", "value2")
          _ <- scanRedis.set("scanKey3", "value3")
          result <- scanRedis.scan(count = Some(1000))
        } yield {
          assert(result.index == 0)
          assert(result.data.sorted == Seq("scanKey1", "scanKey2", "scanKey3"))
        }
        Await.result(r, timeOut)
      })
    }

    // @see https://gist.github.com/jacqui/983051
    "SORT" in {
      val init = Future.sequence(
        Seq(
          redis.hset("bonds|1", "bid_price", 96.01),
          redis.hset("bonds|1", "ask_price", 97.53),
          redis.hset("bonds|2", "bid_price", 95.50),
          redis.hset("bonds|2", "ask_price", 98.25),
          redis.del("bond_ids"),
          redis.sadd("bond_ids", 1),
          redis.sadd("bond_ids", 2),
          redis.del("sortAlpha"),
          redis.rpush("sortAlpha", "abc", "xyz")
        )
      )
      val r = for {
        _ <- init
        sort <- redis.sort("bond_ids")
        sortDesc <- redis.sort("bond_ids", order = Some(DESC))
        sortAlpha <- redis.sort("sortAlpha", alpha = true)
        sortLimit <- redis.sort("bond_ids", limit = Some(LimitOffsetCount(0, 1)))
        b1 <- redis.sort("bond_ids", byPattern = Some("bonds|*->bid_price"))
        b2 <- redis.sort("bond_ids", byPattern = Some("bonds|*->bid_price"), getPatterns = Seq("bonds|*->bid_price"))
        b3 <- redis.sort("bond_ids", Some("bonds|*->bid_price"), getPatterns = Seq("bonds|*->bid_price", "#"))
        b4 <- redis.sort("bond_ids", Some("bonds|*->bid_price"), Some(LimitOffsetCount(0, 1)))
        b5 <- redis.sort("bond_ids", Some("bonds|*->bid_price"), order = Some(DESC))
        b6 <- redis.sort("bond_ids", Some("bonds|*->bid_price"))
        b7 <- redis.sortStore("bond_ids", Some("bonds|*->ask_price"), store = "bond_ids_sorted_by_ask_price")
      } yield {
        assert(sort == Seq(ByteString("1"), ByteString("2")))
        assert(sortDesc == Seq(ByteString("2"), ByteString("1")))
        assert(sortAlpha == Seq(ByteString("abc"), ByteString("xyz")))
        assert(sortLimit == Seq(ByteString("1")))
        assert(b1 == Seq(ByteString("2"), ByteString("1")))
        assert(b2 == Seq(ByteString("95.5"), ByteString("96.01")))
        assert(b3 == Seq(ByteString("95.5"), ByteString("2"), ByteString("96.01"), ByteString("1")))
        assert(b4 == Seq(ByteString("2")))
        assert(b5 == Seq(ByteString("1"), ByteString("2")))
        assert(b6 == Seq(ByteString("2"), ByteString("1")))
        assert(b7 == 2)
      }
      Await.result(r, timeOut)
    }

    "TTL" in {
      val r = for {
        s <- redis.set("ttlKey", "value")
        e <- redis.expire("ttlKey", 10)
        ttl <- redis.ttl("ttlKey")
      } yield {
        assert(s)
        assert(e)
        assert((1 <= ttl.toInt) && (ttl.toInt <= 10))
      }
      Await.result(r, timeOut)
    }

    "TYPE" in {
      val r = for {
        s <- redis.set("typeKey", "value")
        _type <- redis.`type`("typeKey")
        _typeNone <- redis.`type`("typeKeyNonExisting")
      } yield {
        assert(s)
        assert(_type == "string")
        assert(_typeNone == "none")
      }
      Await.result(r, timeOut)
    }

    "UNLINK" in {
      val r = for {
        s <- redis.set("unlinkKey", "value")
        d <- redis.unlink("unlinkKey", "unlinkKeyNonexisting")
      } yield {
        assert(s)
        assert(d == 1)
      }
      Await.result(r, timeOut)
    }
  }
}
