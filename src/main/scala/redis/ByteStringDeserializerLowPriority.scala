package redis

import org.apache.pekko.util.ByteString

trait ByteStringDeserializerLowPriority extends ByteStringDeserializerDefault {

  given ByteString: ByteStringDeserializer[ByteString] =
    bs => bs

}
