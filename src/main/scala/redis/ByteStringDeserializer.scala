package redis

import redis.RediscalaCompat.util.ByteString
import scala.annotation.implicitNotFound

object ByteStringDeserializer extends ByteStringDeserializerLowPriority

@implicitNotFound(msg = "No ByteString deserializer found for type ${T}. Try to implement an implicit ByteStringDeserializer for this type.")
trait ByteStringDeserializer[T] { self =>
  def deserialize(bs: ByteString): T

  def map[A](f: T => A): ByteStringDeserializer[A] =
    (bs: ByteString) => f(self.deserialize(bs))
}
