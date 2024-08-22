package redis.api

sealed trait ListPivot extends Product with Serializable

case object AFTER extends ListPivot

case object BEFORE extends ListPivot
