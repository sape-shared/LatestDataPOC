package com.mongopoc.crud

import java.io.File
import java.sql.Date
import java.util.Calendar

import com.mongodb.client.model.{Filters => MongoFilters}
import com.mongodb.{MongoClient, MongoClientURI}
import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import com.mongopoc.commons.Constants._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory._
import org.apache.spark.sql.SparkSession
import org.bson.Document
import org.mongodb.scala.model.Projections

import scala.collection.JavaConverters._

object JoinRiskTradeDataQuery  extends App{

  val bookIdList = List("3553").mkString( "[\"", "\",\"", "\"]")
  val configFilePath = args(0)
  val config: Config = {
    parseFile(new File(configFilePath))
  }

  val spark = SparkSession.builder()
    .master(config.getString("configuration.spark.master.url"))
    .appName("Bulk Ingestion Mongo POC")
    .getOrCreate()
  val startTime = Calendar.getInstance().getTimeInMillis
  val riskFindPropertyMap: Map[String, String] = Map(DB_NAME -> "HSBC" , MONGO_COLLECTION -> "riskmeasure")//, "SNAPSHOT"->"20170210120000")
  val mongoDSLUri = "mongodb://localhost:27017"
  val mongoRSLUri = "mongodb://localhost:27017/HSBC.riskmeasure"

  val readDSLConfig = ReadConfig(Map(MONGO_URI->mongoDSLUri, "database"->"HSBC", "collection"->"trades"))
  import spark.implicits._
  val tempStr = "{ $match: { \"index.booktradeId\" : { $in : ["+ bookIdList +"]  } } }"
  val tradeIdsRDD = MongoSpark.load(spark.sparkContext,readDSLConfig).withPipeline(Seq(Document.parse("{ $match: { \"index.booktradeId\" : { $in : "+ bookIdList +"  } } }"), Document.parse("{ $project : { \"index.tradetradeId\" : 1} }"))).map(doc => doc.get[Document]("index",classOf[Document]).getString("tradetradeId")).repartition(3)
  val mongoRslURIBr = spark.sparkContext.broadcast(mongoRSLUri)
  val propertyMapBr = spark.sparkContext.broadcast(riskFindPropertyMap)
  val duration = Calendar.getInstance().getTimeInMillis -startTime
  println(s"Time taken in fetchin trade ids from DSL : ${duration}" )

  val riskData = tradeIdsRDD.mapPartitions{iter =>
    val mongoUri = mongoRslURIBr.value
    val mongoPropertyMap = propertyMapBr.value
    val mongoRSLClient = new MongoClient(new MongoClientURI(mongoUri))
    val collection= mongoRSLClient.getDatabase("HSBC").getCollection("riskmeasure")

    val filter = MongoFilters.and(MongoFilters.in("riskSource.tradeId", iter.toList:_*), MongoFilters.eq("valuationDate", 20170111L))
    val doc = collection.find(filter)//.projection(Projections.include("riskSource"))
    doc.iterator().asScala
  }
  val riskCount = riskData.count()
  val readRiskDuration = Calendar.getInstance().getTimeInMillis -startTime
  println(s"Time taken in fetchin risk from from RSL : ${readRiskDuration}" )
  riskData.foreach(x => println(x))
}
