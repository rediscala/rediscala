package redis

import redis.RediscalaCompat.util.ByteString

trait ByteStringFormatterDefault {
  implicit def GenericFormatter[T](implicit s: ByteStringSerializer[T], d: ByteStringDeserializer[T]): ByteStringFormatter[T] =
    new ByteStringFormatter[T] {
      override def serialize(data: T): ByteString = s.serialize(data)
      override def deserialize(bs: ByteString): T = d.deserialize(bs)
    }
}
