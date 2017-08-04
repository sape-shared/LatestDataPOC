package com.rsl.util

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.lit
import com.rsl.commons.Constants._
import com.mongodb.client.model.UpdateManyModel
import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.WriteConfig
import com.mongodb.{MongoClient, ServerAddress}
import com.rsl.commons.SparkSessionProvider
import org.apache.spark.broadcast.Broadcast
import org.bson.Document

/**
  * Created by sgar42 on 04-Aug-17.
  */
object LoadL3NestedParquetDataIntoMongoAtlas extends SparkSessionProvider {

  def main(args: Array[String]): Unit = {
    println(args(0))
    val (rawDataLocation, tradeIdBatchSize, collection, mongo_host, mongo_port, num_partitions, valuationDate, expireOldRecords) = (args(0), args(1), args(2), args(3), args(4), args(5), args(6), args(7))
    val propertyMap: Map[String, String] = Map(VALUATION_DATE -> valuationDate, TRADE_ID_BATCH_SIZE -> tradeIdBatchSize, MONGO_COLLECTION -> collection, MONGO_HOST -> mongo_host, MONGO_PORT -> mongo_port, DB_NAME -> "risk", NUM_PARTITIONS -> num_partitions, EXPIRE_OLD_RECORDS -> expireOldRecords)
    val inputRawDataDF: DataFrame = createDFFromRawData(rawDataLocation, propertyMap)
    println(inputRawDataDF.count())
    val doExpire = expireOldRecords match {
      case "false" => false
      case _ => true
    }
    if (doExpire)
      expireOldVersion(inputRawDataDF, propertyMap)

    val mongoServers = mongo_host.replaceAll(",", ":" + mongo_port) + ":" + mongo_port
    val writeConfig = WriteConfig(Map("uri" -> s"mongodb://$mongoServers/risk.$collection"))
    MongoSpark.save(inputRawDataDF, writeConfig)
  }


  def createDFFromRawData(rawDataLocation: String, propertyMap: Map[String, String]): DataFrame = {
    val inputDF = spark.read.json(rawDataLocation)
    val resultantDF = inputDF.drop(VALUATION_DATE).drop(VALID_TO).drop(VALID_FROM).withColumn(VALUATION_DATE, lit(getValuationDate(propertyMap.get(VALUATION_DATE).getOrElse(""))))
      .withColumn(VALID_TO, lit(DEFAULT_VALID_TO_DATE)).withColumn(VALID_FROM, lit(getCurrentTimeStamp()))

    resultantDF
  }


  def getValuationDate(valuationDate: String): Long = {
    var valuation_date = 0L
    if (valuationDate.isEmpty)
      valuation_date = DEFAULT_VALUATION_DATE
    else
      valuation_date = valuationDate.trim.toLong

    valuation_date
  }

  def getCurrentTimeStamp(): String = {
    val timeStampFormat: String = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT).withZone(ZoneId.systemDefault()).format(Instant.now)
    timeStampFormat
  }

  def expireOldVersion(dataFrame: DataFrame, propertyMap: Map[String, String]): Unit = {
    println("In expireOldVersion ")
    import scala.collection.JavaConverters._

    val propertyMap_Broadcast: Broadcast[Map[String, String]] = dataFrame.rdd.context.broadcast(propertyMap)
    dataFrame.sqlContext.setConf("spark.sql.shuffle.partitions", propertyMap.get(NUM_PARTITIONS).get)

    dataFrame.select(RISK_SOURCE_HSBC_TRADE_ID, VALUATION_DATE, VALUATION_CONTEXT_DESCRIPTION, MEASURE_NAME, VALID_FROM)
      .distinct().show(1)

    dataFrame.select(RISK_SOURCE_HSBC_TRADE_ID, VALUATION_DATE, VALUATION_CONTEXT_DESCRIPTION, MEASURE_NAME, VALID_FROM)
      .distinct()
      .foreachPartition { iter =>
        // Partition level declaration
        val propertyMapPartitionLevel = propertyMap_Broadcast.value
        val mongoPort = propertyMapPartitionLevel.get(MONGO_PORT).get.trim.toInt
        val mongoServerAddress: Seq[ServerAddress] = propertyMapPartitionLevel.get(MONGO_HOST).get.split(" ").map { host => new ServerAddress(host.trim, mongoPort) }.toList
        val mongoClient = new MongoClient(mongoServerAddress.asJava)
        val riskCollection = mongoClient.getDatabase(propertyMapPartitionLevel.get(DB_NAME).get).getCollection(propertyMapPartitionLevel.get(MONGO_COLLECTION).get)
        val tradeIDBatchSize = propertyMapPartitionLevel.get(TRADE_ID_BATCH_SIZE)
        println(mongoServerAddress)
        val writesBatch: List[UpdateManyModel[Document]] = iter.map { row =>

          val filter = new Document()
          filter.put(RISK_SOURCE_HSBC_TRADE_ID, row.getAs[String]("hsbctradeId"))
          filter.put(VALUATION_DATE, row.getAs[String]("valuationDate"))
          filter.put(VALUATION_CONTEXT_DESCRIPTION, row.getAs[String]("description"))
          filter.put(MEASURE_NAME, row.getAs[String]("name"))
          filter.put(VALID_TO, DEFAULT_VALID_TO_DATE)

          val newDocument = new Document()
          newDocument.put(VALID_TO, row.getAs[Long](VALID_FROM))

          val updateDocument = new Document()
          updateDocument.put("$set", newDocument)

          new UpdateManyModel[Document](filter, updateDocument)
        }.toList


        println(s"tradeIDBatchSize $tradeIDBatchSize")
        writesBatch.grouped(tradeIDBatchSize.get.toInt).map { writes =>
          riskCollection.bulkWrite(writes.asJava)
        }
      }
  }


}
