package redis

import org.apache.pekko.util.ByteString

trait ByteStringFormatterDefault {
  given GenericFormatter[T](using s: ByteStringSerializer[T], d: ByteStringDeserializer[T]): ByteStringFormatter[T] =
    new ByteStringFormatter[T] {
      override def serialize(data: T): ByteString = s.serialize(data)
      override def deserialize(bs: ByteString): T = d.deserialize(bs)
    }
}
