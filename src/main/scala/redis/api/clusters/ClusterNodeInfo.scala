package redis.api.clusters

case class ClusterNodeInfo(
  id: String,
  ip_port: String,
  flags: String,
  master: String,
  ping_sent: Long,
  pong_recv: Long,
  config_epoch: Long,
  link_state: String,
  slots: Array[String]
)
