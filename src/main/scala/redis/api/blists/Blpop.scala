package redis.api.blists

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import redis.*

case class Blpop[KK: ByteStringSerializer, R: ByteStringDeserializer](keys: Seq[KK], timeout: FiniteDuration = Duration.Zero)
    extends BXpop[KK, R]("BLPOP")
