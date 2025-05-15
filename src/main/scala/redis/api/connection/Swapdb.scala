package redis.api.connection

import org.apache.pekko.util.ByteString
import redis.*

case class Swapdb(index1: Int, index2: Int) extends RedisCommandStatusBoolean {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SWAPDB", Seq(ByteString(index1.toString), ByteString(index2.toString)))
}
