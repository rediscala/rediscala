package redis.protocol

import redis.RediscalaCompat.util.ByteString

object ParseNumber {

  def parseInt(byteString: ByteString): Int = {
    if (byteString == null) {
      throw new NumberFormatException("null")
    }

    java.lang.Integer.parseInt(byteString.utf8String)
  }

  def parseLong(byteString: ByteString): Long = {
    if (byteString == null) {
      throw new NumberFormatException("null")
    }

    java.lang.Long.parseLong(byteString.utf8String)
  }
}
