package redis

import org.apache.pekko.util.ByteString

trait ByteStringSerializerLowPriority {

  implicit object String extends ByteStringSerializer[String] {
    def serialize(key: String): ByteString = ByteString(key)
  }

  implicit object ShortConverter extends ByteStringSerializer[Short] {
    def serialize(i: Short): ByteString = ByteString(i.toString)
  }

  implicit object IntConverter extends ByteStringSerializer[Int] {
    def serialize(i: Int): ByteString = ByteString(i.toString)
  }

  implicit object LongConverter extends ByteStringSerializer[Long] {
    def serialize(i: Long): ByteString = ByteString(i.toString)
  }

  implicit object FloatConverter extends ByteStringSerializer[Float] {
    def serialize(f: Float): ByteString = ByteString(f.toString)
  }

  implicit object DoubleConverter extends ByteStringSerializer[Double] {
    def serialize(d: Double): ByteString = ByteString(d.toString)
  }

  implicit object CharConverter extends ByteStringSerializer[Char] {
    def serialize(c: Char): ByteString = ByteString(c)
  }

  implicit object ByteConverter extends ByteStringSerializer[Byte] {
    def serialize(b: Byte): ByteString = ByteString(b)
  }

  implicit object ArrayByteConverter extends ByteStringSerializer[Array[Byte]] {
    def serialize(b: Array[Byte]): ByteString = ByteString(b)
  }

  implicit object ByteStringConverter extends ByteStringSerializer[ByteString] {
    def serialize(bs: ByteString): ByteString = bs
  }

}
