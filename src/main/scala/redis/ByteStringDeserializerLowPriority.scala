package redis

import org.apache.pekko.util.ByteString

trait ByteStringDeserializerLowPriority extends ByteStringDeserializerDefault {

  implicit object ByteString extends ByteStringDeserializer[ByteString] {
    def deserialize(bs: ByteString): ByteString = bs
  }

}
