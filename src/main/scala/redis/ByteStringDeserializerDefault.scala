package redis

import org.apache.pekko.util.ByteString

trait ByteStringDeserializerDefault {

  given String: ByteStringDeserializer[String] = (_.utf8String)

  given ByteArray: ByteStringDeserializer[Array[Byte]] = (_.toArray)

  given RedisDouble: ByteStringDeserializer[Double] = { (bs: ByteString) =>
    val s = bs.utf8String
    if ("-inf".equals(s))
      Double.NegativeInfinity
    else if ("inf".equals(s))
      Double.PositiveInfinity
    else
      java.lang.Double.parseDouble(s)
  }

}
