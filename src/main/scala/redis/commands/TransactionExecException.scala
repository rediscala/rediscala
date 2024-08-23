package redis.commands

import redis.protocol.*

case class TransactionExecException(reply: RedisReply) extends Exception(s"Expected MultiBulk response, got : $reply")
