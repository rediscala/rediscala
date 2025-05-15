package redis

import java.util.concurrent.atomic.AtomicBoolean
import org.apache.pekko.actor.ActorRef

case class RedisConnection(actor: ActorRef, active: AtomicBoolean = new AtomicBoolean(false))
