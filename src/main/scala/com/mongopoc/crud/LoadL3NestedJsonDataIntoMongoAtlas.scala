package com.mongopoc.crud

import java.time.{Instant, LocalDate, ZoneId}
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.lit
import com.mongopoc.commons.Constants._
import com.mongodb.client.model.UpdateManyModel
import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.WriteConfig
import com.mongodb.{MongoClient, ServerAddress}
import com.mongopoc.commons.{MongoConfigurations, SparkSessionProvider}
import org.apache.hadoop.fs.Path
import org.apache.spark.broadcast.Broadcast
import org.bson.Document

import scala.collection.mutable
import scala.collection.mutable.{ListBuffer => MutableListBuffer}

/**
  * Created by sgar42 on 04-Aug-17.
  */
object LoadL3NestedJsonDataIntoMongoAtlas extends SparkSessionProvider with MongoConfigurations {


  def loadL3DataIntoMongoAtlas(baseInputPath : String, startDate : String, numDays : Int, doExpireOldRecords : Boolean) = {

    val propertyMap: Map[String, String] = createPropertyMap
    val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault())
    val sd = LocalDate.parse(startDate, dateTimeFormat)
    val inputPathList = mutable.MutableList[String]()

    (0 until  numDays).map{ days =>
      val date = sd.plusDays(days).format(dateTimeFormat)
      inputPathList += baseInputPath + Path.SEPARATOR_CHAR + date
    }

    println("Input Locations for Load L3 Data : ")
    inputPathList.foreach(println(_))

    /*while (!sd.isAfter(ed)) {
      inputPathList += baseInputPath + Path.SEPARATOR_CHAR + sd.format(dateTimeFormat)
      sd = sd.plusDays(1)
    }*/

    val inputDF = spark.read.json(inputPathList: _*)

    //val inputRawDataDF: DataFrame = createDFFromRawData(baseInputPath, propertyMap)

    if (doExpireOldRecords)
      expireOldVersion(inputDF, propertyMap)

    val mongoServers = mongo_host.replaceAll(",", ":" + mongo_port) + ":" + mongo_port
    val writeConfig = WriteConfig(Map("uri" -> s"mongodb://$mongoServers/risk.$collection"))
    MongoSpark.save(inputDF, writeConfig)

  }

  def createPropertyMap: Map[String, String] = {
    Map(TRADE_ID_BATCH_SIZE -> tradeIdBatchSize, MONGO_COLLECTION -> collection, MONGO_HOST -> mongo_host, MONGO_PORT -> mongo_port, DB_NAME -> "risk", NUM_PARTITIONS -> num_partitions)
  }

  /*def createDFFromRawData(rawDataLocation: String, propertyMap: Map[String, String]): DataFrame = {
    val inputDF = spark.read.json(rawDataLocation)
    val resultantDF = inputDF.drop(VALID_TO).drop(VALID_FROM).withColumn(VALID_TO, lit(DEFAULT_VALID_TO_DATE)).withColumn(VALID_FROM, lit(getCurrentTimeStamp()))

    resultantDF
  }


  def getCurrentTimeStamp(): String = {
    val timeStampFormat: String = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT).withZone(ZoneId.systemDefault()).format(Instant.now)
    timeStampFormat
  }*/

  def expireOldVersion(dataFrame: DataFrame, propertyMap: Map[String, String]): Unit = {
    import scala.collection.JavaConverters._

    val propertyMap_Broadcast: Broadcast[Map[String, String]] = dataFrame.rdd.context.broadcast(propertyMap)
    dataFrame.sqlContext.setConf("spark.sql.shuffle.partitions", propertyMap.get(NUM_PARTITIONS).get)

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

        iter.grouped(tradeIDBatchSize.get.toInt).foreach {
          groupedRows =>
            val writeBatch: List[UpdateManyModel[Document]] = groupedRows.map { row =>
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
            riskCollection.bulkWrite(writeBatch.asJava)
        }
      }
  }
}
