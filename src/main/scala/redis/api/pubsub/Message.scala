package redis.api.pubsub

import redis.RediscalaCompat.util.ByteString

case class Message(channel: String, data: ByteString)
