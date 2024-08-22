package redis

import redis.RediscalaCompat.util.Helpers
import java.util.concurrent.atomic.AtomicLong

private[redis] object Redis {
  val dispatcher = RedisDispatcher("rediscala.rediscala-client-worker-dispatcher")

  val tempNumber = new AtomicLong

  def tempName() = Helpers.base64(tempNumber.getAndIncrement())

}
