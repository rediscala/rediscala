package redis.protocol

import redis.RediscalaCompat.util.ByteString
import org.scalatest.wordspec.AnyWordSpec

class ParseParse extends AnyWordSpec {
  "parse" should {
    "integer" in {
      val int = ByteString("654\r\n")
      assert(RedisProtocolReply.decodeInteger(int) == FullyDecoded(Integer(ByteString("654")), ByteString()))

      val (intStart, intEnd) = int.splitAt(int.length - 1)

      var result = RedisProtocolReply.decodeInteger(ByteString(intStart.head))
      for {
        b <- intStart.tail
      } yield {
        result = result.run(ByteString(b))
        assert(result.isFullyDecoded == false)
      }

      val decodeResult = result.run(intEnd)
      assert(decodeResult.isFullyDecoded)
      assert(decodeResult == FullyDecoded(Integer(ByteString("654")), ByteString()))
    }

    "decodeBulk" in {
      val bulk = ByteString("6\r\nfoobar\r\n")
      assert(RedisProtocolReply.decodeBulk(bulk) == FullyDecoded(Bulk(Some(ByteString("foobar"))), ByteString()))

      val (bulkStart, bulkEnd) = bulk.splitAt(bulk.length - 1)

      var result = RedisProtocolReply.decodeBulk(ByteString(bulkStart.head))
      for {
        b <- bulkStart.tail
      } yield {
        result = result.run(ByteString(b))
        assert(result.isFullyDecoded == false)
      }

      val decodeResult = result.run(bulkEnd)
      assert(decodeResult.isFullyDecoded)
      assert(decodeResult == FullyDecoded(Bulk(Some(ByteString("foobar"))), ByteString()))
    }

    "multibulk" in {
      val multibulkString = ByteString("*4\r\n$3\r\nfoo\r\n$3\r\nbar\r\n$5\r\nHello\r\n$5\r\nWorld\r\n")

      val (multibulkStringStart, multibulkStringEnd) = multibulkString.splitAt(multibulkString.length - 1)

      var r3 = RedisProtocolReply.decodeReplyMultiBulk(ByteString(multibulkStringStart.head))

      for {
        b <- multibulkStringStart.tail
      } yield {
        r3 = r3.run(ByteString(b))
        assert(r3.isFullyDecoded == false)
      }

      val nextBS = ByteString("*2\r\n$3\r\none\r\n$3\r\ntwo\r\n")
      val result = r3.run(multibulkStringEnd ++ nextBS)
      assert(result.isFullyDecoded)

      val multibulk =
        Some(Vector(Bulk(Some(ByteString("foo"))), Bulk(Some(ByteString("bar"))), Bulk(Some(ByteString("Hello"))), Bulk(Some(ByteString("World")))))
      assert(result == FullyDecoded(MultiBulk(multibulk), nextBS))

      val bs = ByteString("*4\r\n$3\r\none\r\n$1\r\n2\r\n$3\r\ntwo\r\n$1\r\n4\r\n*2\r\n$3\r\ntwo\r\n$5\r\nthree")
      val nextBS2 = ByteString("*2\r\n$3\r\ntwo\r\n$5\r\nthree")

      val r10 = RedisProtocolReply.decodeReplyMultiBulk(bs)
      assert(
        r10 == FullyDecoded(
          MultiBulk(
            Some(Vector(Bulk(Some(ByteString("one"))), Bulk(Some(ByteString("2"))), Bulk(Some(ByteString("two"))), Bulk(Some(ByteString("4")))))
          ),
          nextBS2
        )
      )
    }
  }
}
