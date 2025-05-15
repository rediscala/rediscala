package redis

import org.apache.pekko.util.ByteString
import scala.annotation.implicitNotFound

object ByteStringSerializer extends ByteStringSerializerLowPriority

@implicitNotFound(msg = "No ByteString serializer found for type ${K}. Try to implement an implicit ByteStringSerializer for this type.")
trait ByteStringSerializer[K] { self =>
  def serialize(data: K): ByteString

  def contramap[A](f: A => K): ByteStringSerializer[A] =
    (data: A) => self.serialize(f(data))
}
