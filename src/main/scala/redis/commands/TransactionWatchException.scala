package redis.commands

case class TransactionWatchException(message: String = "One watched key has been modified, transaction has failed") extends Exception(message)
