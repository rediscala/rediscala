package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.*
import scala.concurrent.duration.FiniteDuration

case class Migrate[K](
  host: String,
  port: Int,
  keys: Seq[K],
  destinationDB: Int,
  timeout: FiniteDuration,
  copy: Boolean = false,
  replace: Boolean = false,
  password: Option[String]
)(implicit redisKey: ByteStringSerializer[K])
    extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = {
    val builder = Seq.newBuilder[ByteString]

    builder += ByteString(host)
    builder += ByteString(port.toString)
    builder += ByteString("")
    builder += ByteString(destinationDB.toString)
    builder += ByteString(timeout.toMillis.toString)

    if (copy)
      builder += ByteString("COPY")
    if (replace)
      builder += ByteString("REPLACE")
    if (password.isDefined) {
      builder += ByteString("AUTH")
      builder += ByteString(password.get)
    }

    builder += ByteString("KEYS")
    builder ++= keys.map(redisKey.serialize)

    RedisProtocolRequest.multiBulk("MIGRATE", builder.result())
  }
}
