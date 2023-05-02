package redis.protocol

import org.scalatest.wordspec.AnyWordSpec
import akka.util.ByteString

class ParseNumberSpec extends AnyWordSpec {

  import java.lang.Integer

  "ParseNumber.parseInt" should {
    "ok" in {
      assert(ParseNumber.parseInt(ByteString("0")) == 0)
      assert(ParseNumber.parseInt(ByteString("10")) == 10)
      assert(ParseNumber.parseInt(ByteString("-10")) == -10)
      assert(ParseNumber.parseInt(ByteString("-123456")) == -123456)
      assert(ParseNumber.parseInt(ByteString("1234567890")) == 1234567890)
      assert(ParseNumber.parseInt(ByteString("-1234567890")) == -1234567890)
    }

    "null" in {
      assertThrows[NumberFormatException] { ParseNumber.parseInt(null) }
    }

    "lone \"+\" or \"-\"" in {
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString("+")) }
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString("-")) }
    }

    "invalid first char" in {
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString("$")) }
    }

    "empty" in {
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString.empty) }
    }

    "invalid char" in {
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString("?")) }
    }

    "limit min" in {
      val l1: Long = Integer.MIN_VALUE
      val l = l1 - 1
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString(l.toString)) }
    }

    "limit max" in {
      val l1: Long = Integer.MAX_VALUE
      val l = l1 + 1
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString(l.toString)) }
    }

    "not a number" in {
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString("not a number")) }
    }

    "launch exception before integer overflow" in {
      assertThrows[NumberFormatException] { ParseNumber.parseInt(ByteString("-2147483650")) }
    }

  }
}
