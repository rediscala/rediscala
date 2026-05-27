package redis.api.scripting

import java.io.File
import java.security.MessageDigest
import scala.util.Using

case class RedisScript(script: String) {
  lazy val sha1: String = {
    val messageDigestSha1 = MessageDigest.getInstance("SHA-1")
    messageDigestSha1.digest(script.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}

object RedisScript {
  def fromFile(file: File): RedisScript = {
    val lines = Using.resource(scala.io.Source.fromFile(file))(
      _.mkString.stripMargin.replaceAll("[\n\r]", "")
    )
    RedisScript(lines)
  }

  def fromResource(path: String): RedisScript = {
    val lines = Using.resource(scala.io.Source.fromURL(getClass.getResource(path)))(
      _.mkString.stripMargin.replaceAll("[\n\r]", "")
    )
    RedisScript(lines)
  }
}
