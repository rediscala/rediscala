package redis.api.servers

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.protocol.Bulk

case object ClientList extends RedisCommandBulk[Seq[Map[String, String]]] {
  def isMasterOnly: Boolean = true
  val encodedRequest: ByteString = encode("CLIENT", Seq(ByteString("LIST")))

  def decodeReply(r: Bulk): Seq[Map[String, String]] = r.asOptByteString
    .map(bs => {
      val s = bs.utf8String
      val r = s
        .split('\n')
        .map(line => {
          line
            .split(' ')
            .map(kv => {
              val keyValue = kv.split('=')
              val value = if (keyValue.length > 1) keyValue(1) else ""
              (keyValue(0), value)
            })
            .toMap
        })
        .toSeq
      r
    })
    .getOrElse(Seq.empty)
}
