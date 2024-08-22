package redis.actors

object InvalidRedisReply extends RuntimeException("Could not decode the redis reply (Connection closed)", null, true, false)
