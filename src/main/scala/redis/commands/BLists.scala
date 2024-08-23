package redis.commands

import redis.ByteStringDeserializer
import redis.Request
import redis.api.ListDirection
import scala.concurrent.Future
import scala.concurrent.duration.*
import redis.api.blists.*

/**
 * Blocking commands on the Lists
 */
trait BLists extends Request {

  // TODO Future[Option[(KK, ByteString)]]
  def blpop[R: ByteStringDeserializer](keys: Seq[String], timeout: FiniteDuration = Duration.Zero): Future[Option[(String, R)]] =
    send(Blpop(keys, timeout))

  def brpop[R: ByteStringDeserializer](keys: Seq[String], timeout: FiniteDuration = Duration.Zero): Future[Option[(String, R)]] =
    send(Brpop(keys, timeout))

  def brpoplpush[R: ByteStringDeserializer](source: String, destination: String, timeout: FiniteDuration = Duration.Zero): Future[Option[R]] =
    send(Brpoplpush(source, destination, timeout))

  def blmove[R: ByteStringDeserializer](
    source: String,
    destination: String,
    from: ListDirection,
    to: ListDirection,
    timeout: FiniteDuration = Duration.Zero
  ): Future[Option[R]] =
    send(Blmove(source, destination, from, to, timeout))
}
