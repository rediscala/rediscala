package redis

import org.specs2.mutable.Specification
import akka.util.ByteString
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

class ConverterSpec extends Specification {

  import redis.ByteStringSerializer._

  "ByteStringSerializer" should {
    "String" in {
      String.serialize("super string !") mustEqual ByteString("super string !")
    }

    "Short" in {
      ShortConverter.serialize(123) mustEqual ByteString("123")
    }

    "Int" in {
      IntConverter.serialize(123) mustEqual ByteString("123")
    }

    "Long" in {
      LongConverter.serialize(123) mustEqual ByteString("123")
    }

    "Float" in {
      FloatConverter.serialize(123.123f) mustEqual ByteString("123.123")
    }

    "Double" in {
      DoubleConverter.serialize(123.123456) mustEqual ByteString("123.123456")
    }

    "Char" in {
      CharConverter.serialize('a') mustEqual ByteString('a')
    }

    "Byte" in {
      ByteConverter.serialize(123) mustEqual ByteString(123)
    }

    "ArrayByte" in {
      ArrayByteConverter.serialize(Array[Byte](1, 2, 3)) mustEqual ByteString(Array[Byte](1, 2, 3))
    }

    "ByteString" in {
      ByteStringConverter.serialize(ByteString("stupid")) mustEqual ByteString("stupid")
    }
  }

  "ByteStringFormatter" should {
    "DumbClass" in {
      val dumb = DumbClass("aa", "bb")

      val formatter = implicitly[ByteStringFormatter[DumbClass]]

      formatter.serialize(dumb) mustEqual ByteString("aa|bb")
      formatter.deserialize(ByteString("aa|bb")) mustEqual dumb
    }

    "generate from the serializer and the deserializer automatically" in {
      val formatter = implicitly[ByteStringFormatter[Double]]

      val bs = ByteString("123.123456")
      val d = 123.123456
      formatter.serialize(d) mustEqual bs
      formatter.deserialize(bs) mustEqual d
    }
  }

}
