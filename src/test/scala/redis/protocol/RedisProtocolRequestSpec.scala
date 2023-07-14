package redis.protocol

import redis.RediscalaCompat.util.ByteString
import org.scalatest.wordspec.AnyWordSpec

class RedisProtocolRequestSpec extends AnyWordSpec {

  "Encode request" should {
    "inline" in {
      assert(RedisProtocolRequest.inline("PING") == ByteString("PING\r\n"))
    }
    "multibulk" in {
      val encoded = RedisProtocolRequest.multiBulk("SET", Seq(ByteString("mykey"), ByteString("myvalue")))
      assert(encoded == ByteString("*3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$7\r\nmyvalue\r\n"))
    }
  }

}
