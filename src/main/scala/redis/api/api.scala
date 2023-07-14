package redis.api

import redis.RediscalaCompat.util.ByteString

trait Aggregate

case object SUM extends Aggregate

case object MIN extends Aggregate

case object MAX extends Aggregate

case class Limit(value: Double, inclusive: Boolean = true) {
  def toByteString: ByteString = ByteString(if (inclusive) value.toString else "(" + value.toString)
}

trait Order

case object ASC extends Order

case object DESC extends Order

case class LimitOffsetCount(offset: Long, count: Long) {
  def toByteString: Seq[ByteString] = Seq(ByteString("LIMIT"), ByteString(offset.toString), ByteString(count.toString))
}

sealed trait BitOperator extends Product with Serializable

case object AND extends BitOperator

case object OR extends BitOperator

case object XOR extends BitOperator

case object NOT extends BitOperator

sealed trait ListPivot extends Product with Serializable

case object AFTER extends ListPivot

case object BEFORE extends ListPivot

sealed trait ShutdownModifier extends Product with Serializable

case object SAVE extends ShutdownModifier

case object NOSAVE extends ShutdownModifier

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
