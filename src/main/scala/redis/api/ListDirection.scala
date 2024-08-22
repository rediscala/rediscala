package redis.api

sealed abstract class ListDirection(private[redis] val value: String) extends Product with Serializable

object ListDirection {
  case object Left extends ListDirection("LEFT")
  case object Right extends ListDirection("RIGHT")
}
