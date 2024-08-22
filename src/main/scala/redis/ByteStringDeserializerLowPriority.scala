package redis

import redis.RediscalaCompat.util.ByteString

trait ByteStringDeserializerLowPriority extends ByteStringDeserializerDefault {

  implicit object ByteString extends ByteStringDeserializer[ByteString] {
    def deserialize(bs: ByteString): ByteString = bs
  }

}
