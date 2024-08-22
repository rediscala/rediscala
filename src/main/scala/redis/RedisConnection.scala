package redis

import redis.RediscalaCompat.actor.ActorRef
import java.util.concurrent.atomic.AtomicBoolean

case class RedisConnection(actor: ActorRef, active: AtomicBoolean = new AtomicBoolean(false))
