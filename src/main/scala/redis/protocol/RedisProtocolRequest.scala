package redis.protocol

import java.lang.System.arraycopy
import java.nio.charset.StandardCharsets
import org.apache.pekko.util.ByteString

object RedisProtocolRequest {
  val LS_STRING = "\r\n"
  val LS: Array[Byte] = LS_STRING.getBytes(StandardCharsets.UTF_8)

  def multiBulk(command: String, args: Seq[ByteString]): ByteString = {
    val argsSizeString = (args.size + 1).toString
    var length: Int = 1 + argsSizeString.length + LS.length

    val cmdLengthString = command.length.toString

    length += 1 + cmdLengthString.length + LS.length + command.length + LS.length

    args.foreach(arg => {
      val argLengthString = arg.length.toString
      length += 1 + argLengthString.length + LS.length + arg.length + LS.length
    })

    val bytes: Array[Byte] = new Array(length)
    var i: Int = 0
    bytes.update(i, '*')
    i += 1
    arraycopy(argsSizeString.getBytes(StandardCharsets.UTF_8), 0, bytes, i, argsSizeString.length)
    i += argsSizeString.length
    arraycopy(LS, 0, bytes, i, LS.length)
    i += LS.length

    bytes.update(i, '$')
    i += 1
    arraycopy(cmdLengthString.getBytes(StandardCharsets.UTF_8), 0, bytes, i, cmdLengthString.length)
    i += cmdLengthString.length
    arraycopy(LS, 0, bytes, i, LS.length)
    i += LS.length
    arraycopy(command.getBytes(StandardCharsets.UTF_8), 0, bytes, i, command.length)
    i += command.length
    arraycopy(LS, 0, bytes, i, LS.length)
    i += LS.length

    args.foreach(arg => {
      bytes.update(i, '$')
      i += 1

      val argLengthString = arg.length.toString
      arraycopy(argLengthString.getBytes(StandardCharsets.UTF_8), 0, bytes, i, argLengthString.length)
      i += argLengthString.length
      arraycopy(LS, 0, bytes, i, LS.length)
      i += LS.length

      val argArray = arg.toArray
      arraycopy(argArray, 0, bytes, i, argArray.length)
      i += argArray.length

      arraycopy(LS, 0, bytes, i, LS.length)
      i += LS.length
    })
    ByteString(bytes)
  }

  def inline(command: String): ByteString = ByteString(command + LS_STRING)
}
