package redis.api.pubsub

import redis.RediscalaCompat.util.ByteString

case class PMessage(patternMatched: String, channel: String, data: ByteString)
