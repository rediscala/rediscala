package redis.actors

import scala.collection.mutable
import redis.Operation

case class QueuePromises(queue: mutable.Queue[Operation[?, ?]])
