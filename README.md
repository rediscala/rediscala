rediscala
=========

[![scaladex](https://index.scala-lang.org/rediscala/rediscala/rediscala/latest-by-scala-version.svg)](https://index.scala-lang.org/rediscala/rediscala/rediscala)

[![maven](https://img.shields.io/maven-central/v/io.github.rediscala/rediscala_3)](https://search.maven.org/artifact/io.github.rediscala/rediscala_3)

A [Redis](https://redis.io/) client for Scala with non-blocking and asynchronous I/O operations.

 * Reactive : Redis requests/replies are wrapped in Futures.

 * Typesafe : Redis types are mapped to Scala types.

 * Fast : Rediscala uses redis pipelining. Blocking redis commands are moved into their own connection. 
A worker actor handles I/O operations (I/O bounds), another handles decoding of Redis replies (CPU bounds).

### Set up your project dependencies

If you use SBT, you just have to edit `build.sbt` and add the following:

```scala
libraryDependencies += "io.github.rediscala" %% "rediscala" % "<version>"
```

<details><summary>old versions</summary>

From version 1.9.0: 
 * use akka 2.5.23
 * released for scala
   * 2.11
   * 2.12
   * 2.13
```scala
libraryDependencies += "com.github.etaty" %% "rediscala" % "1.9.0"
```

From version 1.8.0: 
 * use akka 2.4.12 (java 1.8)
 * released for scala 2.11 & 2.12
```scala
libraryDependencies += "com.github.etaty" %% "rediscala" % "1.8.0"
```

From version 1.3.1: 
 * use akka 2.3
 * released for scala 2.10 & 2.11
```scala
libraryDependencies += "com.github.etaty" %% "rediscala" % "1.7.0"
```

</details>

### Connect to the database

```scala
import redis.RedisClient
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.apache.pekko.actor.ActorSystem

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val redis = RedisClient()

    val futurePong = redis.ping()
    println("Ping sent!")
    futurePong.map(pong => {
      println(s"Redis replied with a $pong")
    })
    Await.result(futurePong, 5.seconds)

    system.shutdown()
  }
}
```

### Basic Example

https://github.com/rediscala/rediscala-demo

You can fork with : `git clone git@github.com:rediscala/rediscala-demo.git` then run it, with `sbt run`


### Redis Commands

All commands are supported :
* [Keys](https://redis.io/docs/latest/commands/?group=generic) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Keys.html))
* [Strings](https://redis.io/docs/latest/commands/?group=string) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Strings.html))
* [Hashes](https://redis.io/docs/latest/commands/?group=hash) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Hashes.html))
* [Lists](https://redis.io/docs/latest/commands/?group=list)
  * non-blocking ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Lists.html))
  * blocking ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/BLists.html))
* [Sets](https://redis.io/docs/latest/commands/?group=set) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Sets.html))
* [Sorted Sets](https://redis.io/docs/latest/commands/?group=sorted_set) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/SortedSets.html))
* [Pub/Sub](https://redis.io/docs/latest/commands/?group=pubsub) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Publish.html))
* [Transactions](https://redis.io/docs/latest/commands/?group=transactions) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Transactions.html))
* [Connection](https://redis.io/docs/latest/commands/?group=connection) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Connection.html))
* [Scripting](https://redis.io/docs/latest/commands/?group=scripting) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Scripting.html))
* [Server](https://redis.io/docs/latest/commands/?group=server) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/Server.html))
* [HyperLogLog](https://redis.io/docs/latest/commands/?group=hyperloglog) ([scaladoc](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/HyperLogLog.html))

### Blocking commands

[RedisBlockingClient](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/RedisBlockingClient.html) is the instance allowing access to blocking commands :
* blpop
* brpop
* brpopplush

```scala
  redisBlocking.blpop(Seq("workList", "otherKeyWithWork"), 5.seconds).map(result => {
    result.map({
      case (key, work) => println(s"list $key has work : ${work.utf8String}")
    })
  })
```
Full example: [ExampleRediscalaBlocking](https://github.com/rediscala/rediscala-demo/blob/28546daccc39ac9fbf307932679a4d416a6df1cc/src/main/scala/ExampleRediscalaBlocking.scala)

You can fork with: `git clone git@github.com:rediscala/rediscala-demo.git` then run it, with `sbt run`


### Transactions

The idea behind transactions in Rediscala is to start a transaction outside of a redis connection.
We use the [TransactionBuilder](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/commands/TransactionBuilder.html) to store call to redis commands (and for each command we give back a future).
When `exec` is called, `TransactionBuilder` will build and send all the commands together to the server. Then the futures will be completed.
By doing that we can use a normal connection with pipelining, and avoiding to trap a command from outside, in the transaction...

```scala
  val redisTransaction = redis.transaction() // new TransactionBuilder
  redisTransaction.watch("key")
  val set = redisTransaction.set("key", "abcValue")
  val decr = redisTransaction.decr("key")
  val get = redisTransaction.get("key")
  redisTransaction.exec()
```

Full example: [ExampleTransaction](https://github.com/rediscala/rediscala-demo/blob/28546daccc39ac9fbf307932679a4d416a6df1cc/src/main/scala/ExampleTransaction.scala)

You can fork with : `git clone git@github.com:rediscala/rediscala-demo.git` then run it, with `sbt run`

[TransactionsSpec](https://github.com/rediscala/rediscala/blob/8b4ff3bb686d03397401f30da4858f108b9f6dec/src/test/scala/redis/commands/TransactionsSpec.scala) will reveal even more gems of the API.

### Pub/Sub

You can use a case class with callbacks [RedisPubSub](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/RedisPubSub.html)
or extend the actor [RedisSubscriberActor](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/actors/RedisSubscriberActor.html) as shown in the example below

```scala
object ExamplePubSub {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = org.apache.pekko.actor.ActorSystem()

    val redis = RedisClient()

    // publish after 2 seconds every 2 or 5 seconds
    system.scheduler.schedule(2.seconds, 2.seconds)(redis.publish("time", System.currentTimeMillis()))
    system.scheduler.schedule(2.seconds, 5.seconds)(redis.publish("pattern.match", "pattern value"))
    // shutdown pekko in 20 seconds
    system.scheduler.scheduleOnce(20.seconds)(system.shutdown())

    val channels = Seq("time")
    val patterns = Seq("pattern.*")
    // create SubscribeActor instance
    system.actorOf(Props(classOf[SubscribeActor], channels, patterns).withDispatcher("rediscala.rediscala-client-worker-dispatcher"))
  }
}

class SubscribeActor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil) extends RedisSubscriberActor(channels, patterns) {
  override val address: InetSocketAddress = new InetSocketAddress("localhost", 6379)

  def onMessage(message: Message) {
    println(s"message received: $message")
  }

  def onPMessage(pmessage: PMessage) {
    println(s"pattern message received: $pmessage")
  }
}
```

Full example: [ExamplePubSub](https://github.com/rediscala/rediscala-demo/blob/28546daccc39ac9fbf307932679a4d416a6df1cc/src/main/scala/ExamplePubSub.scala)

You can fork with : `git clone git@github.com:rediscala/rediscala-demo.git` then run it, with `sbt run`

[RedisPubSubSpec](https://github.com/rediscala/rediscala/blob/8b4ff3bb686d03397401f30da4858f108b9f6dec/src/test/scala/redis/RedisPubSubSpec.scala) will reveal even more gems of the API.

### Scripting

`RedisScript` is a helper, you can put your LUA script inside and it will compute the hash. 
You can use it with `evalshaOrEval` which run your script even if it wasn't already loaded.

```scala
  val redis = RedisClient()

  val redisScript = RedisScript("return 'rediscala'")

  val r = redis.evalshaOrEval(redisScript).map({
    case b: Bulk => println(b.toString())
  })
  Await.result(r, 5.seconds)
```

Full example: [ExampleScripting](https://github.com/rediscala/rediscala-demo/blob/28546daccc39ac9fbf307932679a4d416a6df1cc/src/main/scala/ExampleScripting.scala)

### Redis Sentinel

[SentinelClient](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/SentinelClient.html) connect to a redis sentinel server.

[SentinelMonitoredRedisClient](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/SentinelMonitoredRedisClient.html) connect to a sentinel server to find the master addresse then start a connection. In case the master change your RedisClient connection will automatically connect to the new master server.
If you are using a blocking client, you can use [SentinelMonitoredRedisBlockingClient](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/SentinelMonitoredRedisBlockingClient.html)

### Pool

[RedisClientPool](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/RedisClientPool.html) connect to a pool of redis servers.
Redis commands are dispatched to redis connection in a round robin way.

### Master Slave

[RedisClientMasterSlaves](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/RedisClientMasterSlaves.html) connect to a master and a pool of slaves.
The `write` commands are sent to the master, while the read commands are sent to the slaves in the [RedisClientPool](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/RedisClientPool.html)

### Config Which Dispatcher to Use

By default, the actors in this project will use the dispatcher `rediscala.rediscala-client-worker-dispatcher`. If you want to use another dispatcher, just config the implicit value of `redisDispatcher`:

```scala
implicit val redisDispatcher = RedisDispatcher("pekko.actor.default-dispatcher")
```

### ByteStringSerializer ByteStringDeserializer ByteStringFormatter

[ByteStringSerializer](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/ByteStringSerializer.html)

[ByteStringDeserializer](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/ByteStringDeserializer.html)

[ByteStringFormatter](https://javadoc.io/doc/io.github.rediscala/rediscala_3/1.15.0-pekko/redis/ByteStringFormatter.html)

```scala
case class DumbClass(s1: String, s2: String)

object DumbClass {
  implicit val byteStringFormatter: ByteStringFormatter[DumbClass] =
    new ByteStringFormatter[DumbClass] {
      def serialize(data: DumbClass): ByteString = {
        //...
      }

      def deserialize(bs: ByteString): DumbClass = {
        //...
      }
    }
}
//...

  val dumb = DumbClass("s1", "s2")

  val r = for {
    set <- redis.set("dumbKey", dumb)
    getDumbOpt <- redis.get[DumbClass]("dumbKey")
  } yield {
    getDumbOpt.map(getDumb => {
      assert(getDumb == dumb)
      println(getDumb)
    })
  }
```

Full example: [ExampleByteStringFormatter](https://github.com/rediscala/rediscala-demo/blob/28546daccc39ac9fbf307932679a4d416a6df1cc/src/main/scala/ExampleByteStringFormatter.scala)

### Scaladoc

[![Scaladoc](https://javadoc.io/badge2/io.github.rediscala/rediscala_3/javadoc.svg)](https://javadoc.io/doc/io.github.rediscala/rediscala_3)

<details><summary>old versions</summary>
  
[Rediscala scaladoc API (version 1.9)](https://oss.sonatype.org/service/local/repositories/releases/archive/com/github/etaty/rediscala_2.12/1.9.0/rediscala_2.12-1.9.0-javadoc.jar/!/redis/index.html)

[Rediscala scaladoc API (version 1.8)](https://oss.sonatype.org/service/local/repositories/releases/archive/com/github/etaty/rediscala_2.11/1.8.0/rediscala_2.11-1.8.0-javadoc.jar/!/index.html#package)

[Rediscala scaladoc API (version 1.7)](https://oss.sonatype.org/service/local/repositories/releases/archive/com/github/etaty/rediscala_2.11/1.7.0/rediscala_2.11-1.7.0-javadoc.jar/!/index.html#package)

[Rediscala scaladoc API (version 1.6)](https://etaty.github.io/rediscala/1.6.0/api/index.html#package)

[Rediscala scaladoc API (version 1.5)](https://etaty.github.io/rediscala/1.5.0/api/index.html#package)

[Rediscala scaladoc API (version 1.4)](https://etaty.github.io/rediscala/1.4.0/api/index.html#package)

[Rediscala scaladoc API (version 1.3)](https://etaty.github.io/rediscala/1.3/api/index.html#package)

[Rediscala scaladoc API (version 1.2)](https://etaty.github.io/rediscala/1.2/api/index.html#package)

[Rediscala scaladoc API (version 1.1)](https://etaty.github.io/rediscala/1.1/api/index.html#package)

[Rediscala scaladoc API (version 1.0)](https://etaty.github.io/rediscala/1.0/api/index.html#package)

</details>

### Performance

More than 250 000 requests/second

* [benchmark result from scalameter](https://bit.ly/rediscalabench-1-1)

The hardware used is a macbook retina (Intel Core i7, 2.6 GHz, 4 cores, 8 threads, 8GB) running the sun/oracle jvm 1.6

