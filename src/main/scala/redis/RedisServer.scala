package redis

case class RedisServer(
  host: String = "localhost",
  port: Int = 6379,
  username: Option[String] = None,
  password: Option[String] = None,
  db: Option[Int] = None
)
