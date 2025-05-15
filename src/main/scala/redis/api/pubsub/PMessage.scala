package redis.api.pubsub

import org.apache.pekko.util.ByteString

case class PMessage(patternMatched: String, channel: String, data: ByteString)
