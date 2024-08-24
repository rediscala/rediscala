package redis

import redis.util.CRC16

object RedisComputeSlot {
  val MAX_SLOTS = 16384

  def hashSlot(key: String): Int = {
    val indexBegin = key.indexOf("{")
    val keytag = if (indexBegin != -1) {
      val indexEnd = key.indexOf("}", indexBegin)
      if (indexEnd != -1) {
        key.substring(indexBegin + 1, indexEnd)
      } else {
        key
      }
    } else {
      key
    }
    CRC16.crc16(keytag) % MAX_SLOTS
  }

}
