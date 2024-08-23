package redis.commands

import redis.ByteStringSerializer
import redis.Request
import redis.api.publish.Publish as PublishCommand
import scala.concurrent.Future

trait Publish extends Request {
  def publish[V: ByteStringSerializer](channel: String, value: V): Future[Long] =
    send(PublishCommand(channel, value))
}
