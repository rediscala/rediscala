package redis.actors

import redis.Operation
import scala.collection.mutable

case class QueuePromises(queue: mutable.Queue[Operation[?, ?]])
