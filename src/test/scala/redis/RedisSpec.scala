package redis

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.TestKit
import org.apache.pekko.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import redis.RedisServerHelper.*
import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.sys.process.*
import scala.util.Try
import scala.util.control.NonFatal

object RedisServerHelper {
  val redisHost = "127.0.0.1"

  // remove stacktrace when we stop the process
  val processLogger = ProcessLogger(println, Console.err.println)
  val redisServerPath = {
    val tmp =
      if (Option(System.getenv("REDIS_HOME")).isEmpty || System.getenv("REDIS_HOME") == "")
        "/usr/local/bin"
      else
        System.getenv("REDIS_HOME")
    val absolutePath = new java.io.File(tmp).getAbsolutePath
    println("redisServerPath: " + absolutePath)
    absolutePath
  }

  val redisServerCmd = s"$redisServerPath/redis-server"
  val redisServerLogLevel = ""

  val portNumber = new AtomicInteger(10500)
}

abstract class RedisHelper extends TestKit(ActorSystem()) with AnyWordSpecLike with BeforeAndAfterAll {

  import scala.concurrent.duration.*

  given executionContext: ExecutionContext = system.dispatchers.lookup(Redis.dispatcher.name)

  given timeout: Timeout = Timeout(10.seconds)
  val timeOut = 10.seconds
  val longTimeOut = 100.seconds

  override final def beforeAll() = {
    super.beforeAll()
    setup()
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    cleanup()
    super.afterAll()
  }

  def setup(): Unit

  def cleanup(): Unit

}

case class RedisVersion(major: Int, minor: Int, patch: Int) extends Ordered[RedisVersion] {

  import scala.math.Ordered.orderingToOrdered

  override def compare(that: RedisVersion): Int = (this.major, this.minor, this.patch).compare((that.major, that.minor, that.patch))
}

abstract class RedisStandaloneServer extends RedisHelper {

  val redisManager = new RedisManager()

  val server = redisManager.newRedisProcess()

  val port = server.port
  lazy val redis = RedisClient(port = port)

  def withRedisServer[T](block: Int => T): T = {
    val serverProcess = redisManager.newRedisProcess()
    serverProcess.start()
    Thread.sleep(3000) // wait for server start
    val result = Try(block(serverProcess.port))
    serverProcess.stop()
    result.get
  }

  override def setup() = {
    Thread.sleep(3000)
  }

  override def cleanup() = {
    redisManager.stopAll()
  }
}

abstract class RedisSentinelClients(val masterName: String = "mymaster") extends RedisHelper {

  import RedisServerHelper.*

  val masterPort = portNumber.getAndIncrement()
  val slavePort1 = portNumber.getAndIncrement()
  val slavePort2 = portNumber.getAndIncrement()
  val sentinelPort1 = portNumber.getAndIncrement()
  val sentinelPort2 = portNumber.getAndIncrement()

  val sentinelPorts = Seq(sentinelPort1, sentinelPort2)

  lazy val redisClient = RedisClient(port = masterPort)
  lazy val sentinelClient = SentinelClient(port = sentinelPort1)
  lazy val sentinelMonitoredRedisClient =
    SentinelMonitoredRedisClient(master = masterName, sentinels = Seq((redisHost, sentinelPort1), (redisHost, sentinelPort2)))

  val redisManager = new RedisManager()

  val master = redisManager.newRedisProcess(masterPort)
  Thread.sleep(1000)
  val slave1 = redisManager.newSlaveProcess(masterPort, slavePort1)
  val slave2 = redisManager.newSlaveProcess(masterPort, slavePort2)

  val sentinel1 = redisManager.newSentinelProcess(masterName, masterPort, sentinelPort1)
  val sentinel2 = redisManager.newSentinelProcess(masterName, masterPort, sentinelPort2)

  def newSlaveProcess() = {
    redisManager.newSlaveProcess(masterPort)
  }

  def newSentinelProcess() = {
    redisManager.newSentinelProcess(masterName, masterPort)
  }

  override def setup() = {
    Thread.sleep(10000)
  }

  override def cleanup() = {
    redisManager.stopAll()
  }

}

class RedisManager {

  import RedisServerHelper.*

  var processes: Seq[RedisProcess] = Seq.empty

  def newSentinelProcess(masterName: String, masterPort: Int, port: Int = portNumber.getAndIncrement()) = {
    val sentinelProcess = new SentinelProcess(masterName, masterPort, port)
    sentinelProcess.start()
    processes = processes :+ sentinelProcess
    sentinelProcess
  }

  def newSlaveProcess(masterPort: Int, port: Int = portNumber.getAndIncrement()) = {
    val slaveProcess = new SlaveProcess(masterPort, port)
    slaveProcess.start()
    processes = processes :+ slaveProcess
    slaveProcess
  }

  def newRedisProcess(port: Int = portNumber.getAndIncrement()) = {
    val redisProcess = new RedisProcess(port)
    redisProcess.start()
    processes = processes :+ redisProcess
    redisProcess
  }

  def stopAll() = {
    processes.foreach(_.stop())
    Thread.sleep(5000)
  }

}

abstract class RedisClusterClients() extends RedisHelper {

  import RedisServerHelper.*

  var processes: Seq[Process] = Seq.empty
  val fileDir = new java.io.File("/tmp/redis" + System.currentTimeMillis())

  def newNode(port: Int) =
    s"$redisServerCmd --port $port --cluster-enabled yes --cluster-config-file nodes-${port}.conf --cluster-node-timeout 30000 --appendonly yes --appendfilename appendonly-${port}.aof --dbfilename dump-${port}.rdb --logfile ${port}.log --daemonize yes"

  val nodePorts = Seq.fill(6) { portNumber.getAndIncrement() }

  override def setup() = {
    println("Setup")
    fileDir.mkdirs()

    processes = nodePorts.map(s => Process(newNode(s), fileDir).run(processLogger))
    Thread.sleep(2000)
    val nodes = nodePorts.map(s => redisHost + ":" + s).mkString(" ")

    val command = s"$redisServerPath/redis-cli --cluster create ${nodes} --cluster-replicas 1"
    println(command)
    Process(command)
      .run(
        new ProcessIO(
          (writeInput: OutputStream) => {
            //
            Thread.sleep(2000)
            println("yes")
            writeInput.write("yes\n".getBytes)
            writeInput.flush
          },
          (processOutput: InputStream) => {
            Source.fromInputStream(processOutput).getLines().foreach { println }
          },
          (processError: InputStream) => {
            Source.fromInputStream(processError).getLines().foreach { println }
          },
          daemonizeThreads = false
        )
      )
      .exitValue()
    Thread.sleep(5000)

  }

  override def cleanup() = {
    println("Stop begin")
    // cluster shutdown
    nodePorts.foreach { port =>
      val out = new Socket(redisHost, port).getOutputStream
      out.write("SHUTDOWN NOSAVE\n".getBytes)
      out.flush
    }
    Thread.sleep(6000)
    // Await.ready(RedisClient(port = port).shutdown(redis.api.NOSAVE),timeOut) }
    processes.foreach(_.destroy())

    // deleteDirectory()

    println("Stop end")
  }

  def deleteDirectory(): Unit = {
    val fileStream = Files.newDirectoryStream(fileDir.toPath)
    fileStream.iterator().asScala.foreach(Files.delete)
    Files.delete(fileDir.toPath)
  }
}

class RedisProcess(val port: Int) {
  var server: Process = null
  val cmd = s"${redisServerCmd} --port $port ${redisServerLogLevel}"

  def start() = {
    if (server == null)
      server = Process(cmd).run(processLogger)
  }

  def stop() = {
    if (server != null) {
      try {
        val out = new Socket(redisHost, port).getOutputStream
        out.write("SHUTDOWN NOSAVE\n".getBytes)
        out.flush
        out.close()

        Thread.sleep(500)

      } catch {
        case NonFatal(e) => e.printStackTrace()
      } finally {
        server.destroy()
        server = null
      }
    }

  }
}

class SentinelProcess(masterName: String, masterPort: Int, port: Int) extends RedisProcess(port) {

  lazy val sentinelConfPath = {
    val sentinelConf =
      s"""
         |sentinel monitor $masterName $redisHost $masterPort 2
         |sentinel down-after-milliseconds $masterName 5000
         |sentinel parallel-syncs $masterName 1
         |sentinel failover-timeout $masterName 10000
            """.stripMargin

    val sentinelConfFile = java.nio.file.Files.createTempFile("rediscala-sentinel", ".conf")
    java.nio.file.Files.write(sentinelConfFile, sentinelConf.getBytes("UTF-8"))
    sentinelConfFile.toFile.getAbsolutePath
  }

  override val cmd = s"${redisServerCmd} $sentinelConfPath --port $port --sentinel $redisServerLogLevel"
}

class SlaveProcess(masterPort: Int, port: Int) extends RedisProcess(port) {
  override val cmd = s"$redisServerCmd --port $port --slaveof $redisHost $masterPort $redisServerLogLevel"
}
