package redis

import com.dimafeng.testcontainers.GenericContainer
import org.specs2.specification.AfterAll
import scala.concurrent.Future

abstract class RedisDockerServer extends RedisHelper with AfterAll {

  private[this] def exportPort = 6379
  private[this] val container = GenericContainer(
    dockerImage = "redis:5.0.14",
    exposedPorts = Seq(exportPort)
  )

  container.start()

  lazy val port: Int = container.mappedPort(exportPort)
  lazy val redis = RedisClient(port = port)

  def redisVersion(): Future[Option[RedisVersion]] = redis.info("Server").map { info =>
    info
      .split("\r\n")
      .drop(1)
      .flatMap { line =>
        line.split(":") match {
          case Array(key, value) => List(key -> value)
          case _ => List.empty
        }
      }
      .find(_._1 == "redis_version")
      .map(_._2.split("\\.") match {
        case Array(major, minor, patch) => RedisVersion(major.toInt, minor.toInt, patch.toInt)
      })
  }

  def withRedisServer[T](block: Int => T): T = {
    block(port)
  }

  override def afterAll(): Unit = {
    container.stop()
  }

  override def setup(): Unit = ()

  override def cleanup(): Unit = ()

}
