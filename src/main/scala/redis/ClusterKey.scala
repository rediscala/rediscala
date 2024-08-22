package redis

trait ClusterKey {
  def getSlot(): Int
}
