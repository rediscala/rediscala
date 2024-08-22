package redis.api.clusters

case class ClusterSlot(begin: Int, end: Int, master: ClusterNode, slaves: Seq[ClusterNode]) extends Comparable[ClusterSlot] {
  override def compareTo(x: ClusterSlot): Int = {
    this.begin.compare(x.begin)
  }
}
