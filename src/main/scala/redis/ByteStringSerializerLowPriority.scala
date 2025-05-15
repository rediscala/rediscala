package redis

import org.apache.pekko.util.ByteString

trait ByteStringSerializerLowPriority {

  given String: ByteStringSerializer[String] = { key =>
    ByteString(key)
  }

  given ShortConverter: ByteStringSerializer[Short] = { i =>
    ByteString(i.toString)
  }

  given IntConverter: ByteStringSerializer[Int] = { i =>
    ByteString(i.toString)
  }

  given LongConverter: ByteStringSerializer[Long] = { i =>
    ByteString(i.toString)
  }

  given FloatConverter: ByteStringSerializer[Float] = { f =>
    ByteString(f.toString)
  }

  given DoubleConverter: ByteStringSerializer[Double] = { d =>
    ByteString(d.toString)
  }

  given CharConverter: ByteStringSerializer[Char] = { c =>
    ByteString(c)
  }

  given ByteConverter: ByteStringSerializer[Byte] = { b =>
    ByteString(b)
  }

  given ArrayByteConverter: ByteStringSerializer[Array[Byte]] = { b =>
    ByteString(b)
  }

  given ByteStringConverter: ByteStringSerializer[ByteString] = { bs =>
    bs
  }

}
