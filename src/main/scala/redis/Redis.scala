package redis

import java.util.concurrent.atomic.AtomicLong
import redis.RediscalaCompat.util.Helpers

private[redis] object Redis {
  val dispatcher = RedisDispatcher("rediscala.rediscala-client-worker-dispatcher")

  val tempNumber = new AtomicLong

  def tempName() = Helpers.base64(tempNumber.getAndIncrement())

}
