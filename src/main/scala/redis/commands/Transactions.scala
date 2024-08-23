package redis.commands

import redis.*

trait Transactions extends ActorRequest {

  def multi(): TransactionBuilder = transaction()

  def multi(operations: TransactionBuilder => Unit): TransactionBuilder = {
    val builder = transaction()
    operations(builder)
    builder
  }

  def transaction(): TransactionBuilder = TransactionBuilder(redisConnection)

  def watch(watchKeys: String*): TransactionBuilder = {
    val builder = transaction()
    builder.watch(watchKeys*)
    builder
  }

}
