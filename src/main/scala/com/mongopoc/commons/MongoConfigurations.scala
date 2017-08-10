package com.mongopoc.commons

trait MongoConfigurations extends Configuration {

  val tradeIdBatchSize = config.getString("configuration.tradeIdBatchSize")
  val collection = config.getString("configuration.mongo.collection")
  val mongo_host = config.getString("configuration.mongo.host")
  val mongo_port = config.getString("configuration.mongo.port")
  val num_partitions = config.getString("configuration.num.partitions")
}
