package redis.actors

case object NoConnectionException extends RuntimeException("No Connection established", null, true, false)
