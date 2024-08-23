package redis

import java.util.concurrent.atomic.AtomicBoolean
import redis.RediscalaCompat.actor.ActorRef

case class RedisConnection(actor: ActorRef, active: AtomicBoolean = new AtomicBoolean(false))
