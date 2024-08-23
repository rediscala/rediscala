package redis.api.keys

import redis.*
import redis.RediscalaCompat.util.ByteString
import redis.api.LimitOffsetCount
import redis.api.Order

case class Sort[K: ByteStringSerializer, R](
  key: K,
  byPattern: Option[String],
  limit: Option[LimitOffsetCount],
  getPatterns: Seq[String],
  order: Option[Order],
  alpha: Boolean
)(implicit deserializerR: ByteStringDeserializer[R])
    extends RedisCommandMultiBulkSeqByteString[R] {
  def isMasterOnly = true
  val encodedRequest: ByteString = encode("SORT", Sort.buildArgs(key, byPattern, limit, getPatterns, order, alpha))
  val deserializer: ByteStringDeserializer[R] = deserializerR
}

private[redis] object Sort {
  def buildArgs[K, KS](
    key: K,
    byPattern: Option[String],
    limit: Option[LimitOffsetCount],
    getPatterns: Seq[String],
    order: Option[Order],
    alpha: Boolean,
    store: Option[KS] = None
  )(implicit redisKey: ByteStringSerializer[K], bsStore: ByteStringSerializer[KS]): Seq[ByteString] = {
    var args = store.map(dest => List(ByteString("STORE"), bsStore.serialize(dest))).getOrElse(List())
    if (alpha) {
      args = ByteString("ALPHA") :: args
    }
    args = order.map(ord => ByteString(ord.toString) :: args).getOrElse(args)
    args = getPatterns.map(pat => List(ByteString("GET"), ByteString(pat))).toList.flatten ++ args
    args = limit.map(_.toByteString).getOrElse(Seq()).toList ++ args
    args = byPattern.map(ByteString("BY") :: ByteString(_) :: args).getOrElse(args)

    redisKey.serialize(key) :: args
  }
}
