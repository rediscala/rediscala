package redis.actors

case class ReplyErrorException(message: String) extends Exception(message)
