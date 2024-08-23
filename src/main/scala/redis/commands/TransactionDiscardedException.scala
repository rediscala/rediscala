package redis.commands

case object TransactionDiscardedException extends Exception(null, null, true, false)
