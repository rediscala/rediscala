package redis.util

import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

class CRC16Spec extends Properties("CRC16") {
  property("consistent other implementation") = forAll { (str: String) =>
    CRC16.crc16(str) == _root_.redis.clients.jedis.util.JedisClusterCRC16.getCRC16(str)
  }
}
