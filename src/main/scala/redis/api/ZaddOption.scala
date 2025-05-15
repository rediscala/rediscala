package redis.api

import org.apache.pekko.util.ByteString

sealed trait ZaddOption extends Product with Serializable {
  def serialize: ByteString
}

object ZaddOption {

  case object XX extends ZaddOption {
    override def serialize: ByteString = ByteString("XX")
  }

  case object NX extends ZaddOption {
    override def serialize: ByteString = ByteString("NX")
  }

  case object CH extends ZaddOption {
    override def serialize: ByteString = ByteString("CH")
  }

  case object INCR extends ZaddOption {
    override def serialize: ByteString = ByteString("INCR")
  }

}
