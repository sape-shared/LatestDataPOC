package com.mongopoc.commons

/**
  * Created by sgar42 on 04-Aug-17.
  */
object Constants {

  val MONGO_COLLECTION = "collection";
  val MONGO_HOST = "mongo_host"
  val MONGO_PORT = "mongoPort"
  val DB_NAME = "dbName"

  val VALUATION_DATE = "valuationDate"
  val VALID_TO = "validTo"
  val VALID_FROM = "validFrom"
  val DEFAULT_VALUATION_DATE = 20170426L
  val DEFAULT_VALID_TO_DATE = 9999123123595959L
  val TIMESTAMP_FORMAT = "yyyyMMddHHmmss"
  val RISK_SOURCE_HSBC_TRADE_ID = "riskSource.hsbctradeId"
  val RISK_SOURCE_TRADE_ID = "riskSource.tradeId"
  val VALUATION_CONTEXT_DESCRIPTION = "valuationContext.description"
  val MEASURE_NAME = "measure.name"


  val TRADE_ID_BATCH_SIZE = "tradeIdBatchSize";
  val NUM_PARTITIONS = "num_partitions"
  val EXPIRE_OLD_RECORDS = "expireOldRecords"

}
