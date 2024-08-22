package redis

object ByteStringFormatter extends ByteStringFormatterDefault

trait ByteStringFormatter[T] extends ByteStringSerializer[T] with ByteStringDeserializer[T]
