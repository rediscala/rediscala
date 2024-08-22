package redis.api

import redis.RediscalaCompat.util.ByteString

sealed trait ShutdownModifier extends Product with Serializable

case object SAVE extends ShutdownModifier

case object NOSAVE extends ShutdownModifier
