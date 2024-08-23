package redis.protocol

import redis.RediscalaCompat.util.ByteString

sealed trait DecodeResult[+A] {
  def rest: ByteString

  def isFullyDecoded: Boolean

  def foreach(f: A => Unit): DecodeResult[Unit] = this match {
    case p @ PartiallyDecoded(_, _) => PartiallyDecoded(ByteString(), bs => p.f(p.rest ++ bs).foreach(f))
    case fd @ FullyDecoded(_, _) => FullyDecoded(f(fd.result), fd.rest)
  }

  def map[B](f: A => B): DecodeResult[B] = this match {
    case p @ PartiallyDecoded(_, _) => PartiallyDecoded(ByteString(), bs => p.f(p.rest ++ bs).map(f))
    case fd @ FullyDecoded(_, _) => FullyDecoded(f(fd.result), fd.rest)
  }

  def flatMap[B](f: (A, ByteString) => DecodeResult[B]): DecodeResult[B] = this match {
    case p @ PartiallyDecoded(_, _) => PartiallyDecoded(ByteString(), bs => p.f(p.rest ++ bs).flatMap(f))
    case fd @ FullyDecoded(_, _) => f(fd.result, fd.rest)
  }

  def run(next: ByteString): DecodeResult[A] = this match {
    case p @ PartiallyDecoded(_, _) => p.f(p.rest ++ next)
    case fd @ FullyDecoded(_, _) => FullyDecoded(fd.result, fd.rest ++ next)
  }
}

case class PartiallyDecoded[A](rest: ByteString, f: ByteString => DecodeResult[A]) extends DecodeResult[A] {
  override def isFullyDecoded: Boolean = false
}

case class FullyDecoded[A](result: A, rest: ByteString) extends DecodeResult[A] {
  override def isFullyDecoded: Boolean = true
}

object DecodeResult {
  val unit: DecodeResult[Unit] = FullyDecoded((), ByteString.empty)
}
