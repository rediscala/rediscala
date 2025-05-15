package redis

import org.apache.pekko.util.ByteString
import redis.protocol.*
import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

object MultiBulkConverter {

  def toSeqString(reply: MultiBulk): Seq[String] = {
    reply.responses
      .map(r => {
        r.map(_.toString)
      })
      .getOrElse(Seq.empty)
  }

  def toSeqByteString[R](reply: MultiBulk)(using deserializer: ByteStringDeserializer[R]): Seq[R] = {
    reply.responses
      .map(r => {
        r.map(reply => deserializer.deserialize(reply.toByteString))
      })
      .getOrElse(Seq.empty)
  }

  def toSeqOptionByteString[R](reply: MultiBulk)(using deserializer: ByteStringDeserializer[R]): Seq[Option[R]] = {
    reply.responses
      .map(r => {
        r.map(_.asOptByteString.map(deserializer.deserialize))
      })
      .getOrElse(Seq.empty)
  }

  def toSeqTuple2ByteStringDouble[R](reply: MultiBulk)(using deserializer: ByteStringDeserializer[R]): Seq[(R, Double)] = {
    reply.responses.map { r =>
      {
        val s = r.map(_.toByteString)
        val builder = Seq.newBuilder[(R, Double)]
        s.grouped(2).foreach { case Seq(a, b) =>
          builder += ((deserializer.deserialize(a), b.utf8String.toDouble))
        }
        builder.result()
      }
    }.getOrElse(Seq.empty)
  }

  def toClusterSlots(reply: MultiBulk): Option[(ByteString, ByteString)] = {
    reply.responses.map { slot =>
      {
        val elementSeq = slot
        val begin = elementSeq(0).toByteString
        val end = elementSeq(1).toByteString
        (begin, end)
      }
    }
  }

  def toMapString(reply: MultiBulk): Map[String, String] = {
    reply.responses
      .map(bs => {
        val builder = Map.newBuilder[String, String]
        seqtoMapString(bs, builder)
        builder.result()
      })
      .getOrElse(Map.empty)
  }

  @tailrec
  private def seqtoMapString(bsSeq: Seq[RedisReply], acc: mutable.Builder[(String, String), Map[String, String]]): Unit = {
    if (bsSeq.nonEmpty) {
      acc += ((bsSeq.head.asOptByteString.map(_.utf8String).getOrElse(""), bsSeq.tail.head.asOptByteString.map(_.utf8String).getOrElse("")))
      seqtoMapString(bsSeq.tail.tail, acc)
    }
  }

  def toSeqMapString(reply: MultiBulk): Seq[Map[String, String]] = {
    reply.responses.map { s =>
      s.map {
        case m: MultiBulk =>
          m.responses.map { s =>
            val builder = Seq.newBuilder[(String, String)]
            s.grouped(2).foreach { case Seq(a, b) =>
              builder += ((a.toString, b.toString))
            }
            builder.result()
          }.getOrElse(Seq())

        case _ => Seq()
      }.map {
        _.toMap
      }
    }.getOrElse(Seq.empty)
  }

  def toOptionStringByteString[R](reply: MultiBulk)(using deserializer: ByteStringDeserializer[R]): Option[(String, R)] = {
    reply.responses.map { r =>
      r.head.toString -> deserializer.deserialize(r.tail.head.toByteString)
    }
  }

  def toSeqBoolean(reply: MultiBulk): Seq[Boolean] = {
    reply.responses
      .map(r => {
        r.map(_.toString == "1")
      })
      .getOrElse(Seq.empty)
  }

  def toStringsSeq(rd: RedisReply): Seq[String] = rd match {
    case MultiBulk(Some(v)) => v.flatMap(toStringsSeq)
    case Bulk(b) => b.map(_.decodeString("US-ASCII")).toSeq
    case Integer(i) => Seq(i.decodeString("US-ASCII"))
    case _ => Nil
  }

}

trait MultiBulkConverter[A] {
  def to(redisReply: MultiBulk): Try[A]
}
