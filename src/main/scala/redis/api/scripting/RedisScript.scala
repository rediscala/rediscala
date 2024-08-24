package redis.api.scripting

import java.io.File
import java.security.MessageDigest

case class RedisScript(script: String) {
  lazy val sha1: String = {
    val messageDigestSha1 = MessageDigest.getInstance("SHA-1")
    messageDigestSha1.digest(script.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}

object RedisScript {
  def fromFile(file: File): RedisScript = {
    val source = scala.io.Source.fromFile(file)
    val lines =
      try source.mkString.stripMargin.replaceAll("[\n\r]", "")
      finally source.close()
    RedisScript(lines)
  }

  def fromResource(path: String): RedisScript = {
    val source = scala.io.Source.fromURL(getClass.getResource(path))
    val lines =
      try source.mkString.stripMargin.replaceAll("[\n\r]", "")
      finally source.close()
    RedisScript(lines)
  }
}
