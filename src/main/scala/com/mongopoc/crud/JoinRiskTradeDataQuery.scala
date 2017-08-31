package com.mongopoc.crud

import java.io.File

import com.mongodb.client.model.{Filters => MongoFilters}
import com.mongodb.{MongoClient, MongoClientURI}
import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import com.mongopoc.commons.Constants._
import com.mongopoc.commons.{MongoConfigurations, SparkSessionProvider}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.load
import org.apache.spark.sql.SparkSession
import org.bson.Document
import org.mongodb.scala.model.Projections

import scala.collection.JavaConverters._

object JoinRiskTradeDataQuery  extends App{

  val bookIdList: List[String]= List("3553")
  val configFilePath = args(0)
  val config: Config = {
    load(configFilePath)
  }

  val spark = SparkSession.builder()
    .master(config.getString("configuration.spark.master.url"))
    .appName("Bulk Ingestion Mongo POC")
    .getOrCreate()

  val riskFindPropertyMap: Map[String, String] = Map(DB_NAME -> "HSBC" , MONGO_COLLECTION -> "riskmeasure")//, "SNAPSHOT"->"20170210120000")
  val mongoDSLUri = "mongodb://localhost:27017"
  val mongoRSLUri = "mongodb://localhost:27017/HSBC.riskmeasure"

  val readDSLConfig = ReadConfig(Map(MONGO_URI->mongoDSLUri, "database"->"HSBC", "collection"->"trades", "partitioner"->"36"))
  import spark.implicits._
  val tradeIdsRDD = MongoSpark.load(spark.sparkContext,readDSLConfig).filter(doc => bookIdList.contains(doc.get[Document]("index", classOf[Document]).getString("tradebookId"))).map(doc => doc.get[Document]("index",classOf[Document]).getString("tradetradeId")).repartition(36)
  val mongoRslURIBr = spark.sparkContext.broadcast(mongoRSLUri)
  val propertyMapBr = spark.sparkContext.broadcast(riskFindPropertyMap)

  val riskData = tradeIdsRDD.mapPartitions{iter =>
    val mongoUri = mongoRslURIBr.value
    val mongoPropertyMap = propertyMapBr.value
    val mongoRSLClient = new MongoClient(new MongoClientURI(mongoUri))
    val collection= mongoRSLClient.getDatabase("HSBC").getCollection("riskmeasure")

    val filter = MongoFilters.and(MongoFilters.in("riskSource.tradeId", iter.toList:_*), MongoFilters.eq("valuationDate", "20170110"))
    val doc = collection.find(filter).projection(Projections.include("riskSource"))
    val itr= doc.iterator().asScala
    mongoRSLClient.close()
    itr
  }
  riskData.foreach(x => {})
}
