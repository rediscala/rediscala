package redis

case class Cursor[T](index: Int, data: T)
