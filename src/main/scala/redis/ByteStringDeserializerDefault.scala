package redis

import org.apache.pekko.util.ByteString

trait ByteStringDeserializerDefault {

  implicit object String extends ByteStringDeserializer[String] {
    def deserialize(bs: ByteString): String = bs.utf8String
  }

  implicit object ByteArray extends ByteStringDeserializer[Array[Byte]] {
    def deserialize(bs: ByteString): Array[Byte] = bs.toArray
  }

  implicit object RedisDouble extends ByteStringDeserializer[Double] {
    override def deserialize(bs: ByteString): Double = {
      val s = bs.utf8String
      if ("-inf".equals(s))
        Double.NegativeInfinity
      else if ("inf".equals(s))
        Double.PositiveInfinity
      else
        java.lang.Double.parseDouble(s)
    }
  }

}
