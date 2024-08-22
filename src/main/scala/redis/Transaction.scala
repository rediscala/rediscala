package redis

case class Transaction(commands: Seq[Operation[?, ?]])
