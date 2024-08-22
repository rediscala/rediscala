package redis.api

trait Aggregate

case object SUM extends Aggregate

case object MIN extends Aggregate

case object MAX extends Aggregate
