package redis.api

sealed trait BitOperator extends Product with Serializable

case object AND extends BitOperator

case object OR extends BitOperator

case object XOR extends BitOperator

case object NOT extends BitOperator
