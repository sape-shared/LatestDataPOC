package com.mongopoc.commons

trait MongoConfigurations extends Configuration {

  val tradeIdBatchSize = config.getString("configuration.tradeIdBatchSize")
  val collection = config.getString("configuration.mongo.collection")
  val mongo_host = config.getString("configuration.mongo.host")
  val mongo_port = config.getString("configuration.mongo.port")
  val num_partitions = config.getString("configuration.num.partitions")
  val mongoUsername = config.getString("configuration.mongo.username")
  val mongoPassword = config.getString("configuration.mongo.password")
  val mongoDbName = config.getString("configuration.mongo.database")
  val ssl = config.getString("configuration.mongo.ssl")
  val replicaSet = config.getString("configuration.mongo.replicaSet")
  val authSource = config.getString("configuration.mongo.authSource")
  val writeConcernsW = config.getString("configuration.mongo.write.concerns.w")
  val writeConcernsJournaled = config.getString("configuration.mongo.write.concerns.j")
  val writeConcernsTimeout = config.getString("configuration.mongo.write.concerns.wtimeout")
}
