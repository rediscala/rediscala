package redis

import org.scalatest.wordspec.AnyWordSpec
import redis.RediscalaCompat.util.ByteString
import redis.protocol.Bulk
import redis.protocol.RedisReply

case class DumbClass(s1: String, s2: String)

object DumbClass {
  implicit val byteStringFormatter: ByteStringFormatter[DumbClass] = new ByteStringFormatter[DumbClass] {
    def serialize(data: DumbClass): ByteString = {
      ByteString(data.s1 + "|" + data.s2)
    }

    def deserialize(bs: ByteString): DumbClass = {
      val r = bs.utf8String.split('|').toList
      DumbClass(r(0), r(1))
    }
  }

  implicit val redisReplyDeserializer: RedisReplyDeserializer[DumbClass] = new RedisReplyDeserializer[DumbClass] {
    override def deserialize: PartialFunction[RedisReply, DumbClass] = { case Bulk(Some(bs)) =>
      byteStringFormatter.deserialize(bs)
    }
  }
}

class ConverterSpec extends AnyWordSpec {

  import redis.ByteStringSerializer.*

  "ByteStringSerializer" should {
    "String" in {
      assert(String.serialize("super string !") == ByteString("super string !"))
    }

    "Short" in {
      assert(ShortConverter.serialize(123) == ByteString("123"))
    }

    "Int" in {
      assert(IntConverter.serialize(123) == ByteString("123"))
    }

    "Long" in {
      assert(LongConverter.serialize(123) == ByteString("123"))
    }

    "Float" in {
      assert(FloatConverter.serialize(123.123f) == ByteString("123.123"))
    }

    "Double" in {
      assert(DoubleConverter.serialize(123.123456) == ByteString("123.123456"))
    }

    "Char" in {
      assert(CharConverter.serialize('a') == ByteString('a'))
    }

    "Byte" in {
      assert(ByteConverter.serialize(123) == ByteString(123))
    }

    "ArrayByte" in {
      assert(ArrayByteConverter.serialize(Array[Byte](1, 2, 3)) == ByteString(Array[Byte](1, 2, 3)))
    }

    "ByteString" in {
      assert(ByteStringConverter.serialize(ByteString("stupid")) == ByteString("stupid"))
    }
  }

  "ByteStringFormatter" should {
    "DumbClass" in {
      val dumb = DumbClass("aa", "bb")

      val formatter = implicitly[ByteStringFormatter[DumbClass]]

      assert(formatter.serialize(dumb) == ByteString("aa|bb"))
      assert(formatter.deserialize(ByteString("aa|bb")) == dumb)
    }

    "generate from the serializer and the deserializer automatically" in {
      val formatter = implicitly[ByteStringFormatter[Double]]

      val bs = ByteString("123.123456")
      val d = 123.123456
      assert(formatter.serialize(d) == bs)
      assert(formatter.deserialize(bs) == d)
    }
  }

}
