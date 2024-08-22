package redis.api

sealed trait ShutdownModifier extends Product with Serializable

case object SAVE extends ShutdownModifier

case object NOSAVE extends ShutdownModifier
