package redis.api.pubsub

import org.apache.pekko.util.ByteString

case class Message(channel: String, data: ByteString)
