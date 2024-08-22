package redis.api.scripting

import redis.*
import redis.RediscalaCompat.util.ByteString

trait EvaledScript {
  def isMasterOnly = true
  def encodeRequest[KK, KA](
    encoder: (String, Seq[ByteString]) => ByteString,
    command: String,
    param: String,
    keys: Seq[KK],
    args: Seq[KA],
    keySerializer: ByteStringSerializer[KK],
    argSerializer: ByteStringSerializer[KA]
  ): ByteString = {
    encoder(
      command,
      (ByteString(param)
        +: ByteString(keys.length.toString)
        +: keys.map(keySerializer.serialize)) ++ args.map(argSerializer.serialize)
    )
  }
}
