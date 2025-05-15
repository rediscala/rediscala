package redis.api.connection

import org.apache.pekko.util.ByteString
import redis.*

case class Select(index: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SELECT", Seq(ByteString(index.toString)))
}
