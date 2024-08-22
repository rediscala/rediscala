package redis

import redis.commands.*

trait RedisCommands
    extends Keys
    with Strings
    with Hashes
    with Lists
    with Sets
    with SortedSets
    with Publish
    with Scripting
    with Connection
    with Server
    with HyperLogLog
    with Clusters
    with Geo
