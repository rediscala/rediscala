package redis.commands

import redis.ByteStringSerializer
import redis.Request
import scala.concurrent.Future
import redis.api.publish.Publish as PublishCommand

trait Publish extends Request {
  def publish[V: ByteStringSerializer](channel: String, value: V): Future[Long] =
    send(PublishCommand(channel, value))
}
