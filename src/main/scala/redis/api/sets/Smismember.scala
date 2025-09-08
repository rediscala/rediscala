package redis.api.sets

import org.apache.pekko.util.ByteString
import redis.*
import redis.protocol.MultiBulk

case class Smismember[K, V](key: K, members: Seq[V])(using redisKey: ByteStringSerializer[K], convert: ByteStringSerializer[V])
    extends SimpleClusterKey[K]
    with RedisCommandMultiBulk[Seq[Boolean]] {
  def isMasterOnly = false
  val encodedRequest: ByteString = encode("SMISMEMBER", keyAsString +: members.map(convert.serialize))
  def decodeReply(mb: MultiBulk): Seq[Boolean] = MultiBulkConverter.toSeqBoolean(mb)
}
