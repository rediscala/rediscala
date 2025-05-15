package redis.protocol

import org.apache.pekko.util.ByteString
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

object ParseNumberScalaCheck extends Properties("ParseNumber") {
  property("parse long") = forAll { (a: Long) =>
    val s = a.toString
    ParseNumber.parseLong(ByteString(s)) == s.toLong
  }

  property("parse int") = forAll { (a: Int) =>
    val s = a.toString
    ParseNumber.parseInt(ByteString(s)) == s.toInt
  }
}
