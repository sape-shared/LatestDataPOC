package com.mongopoc.crud

import com.mongodb.{MongoClient, MongoClientURI}
import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import com.mongopoc.commons.Constants._
import com.mongopoc.commons.{MongoConfigurations, SparkSessionProvider}
import org.mongodb.scala.model.Filters.{and, gt, in, lt}

object JoinRiskTradeDataQuery  extends SparkSessionProvider with App{

  val bookIdList: List[String]= args(0).split(",").toList

  val riskFindPropertyMap: Map[String, String] = Map(DB_NAME -> "HSBC" , MONGO_COLLECTION -> "riskmeasure")//, "SNAPSHOT"->"20170210120000")
  val mongoDSLUri = "mongodb://localhost:27017/HSBC.trades"//s"${mongo_host}:${mongo_port}/${dslDbName}.${dslCollection}"
  val mongoRSLUri = "mongodb://localhost:27017/HSBC.riskmeasure"//s"${mongo_host}:${mongo_port}/${mongoDbName}.${collection}"

  val readDSLConfig = ReadConfig(Map(MONGO_URI->mongoDSLUri))
  val readRSLConfig = ReadConfig(Map(MONGO_URI->mongoRSLUri))

  import spark.implicits._
  val tradeIdsRDD = MongoSpark.load(spark.sparkContext,readDSLConfig).filter(doc => bookIdList.contains(doc.getString("index.bookId"))).map(doc => doc.getString("index.tradeId")).repartition(36)
  val mongoRslURIBr = spark.sparkContext.broadcast(mongoRSLUri)
  val propertyMapBr = spark.sparkContext.broadcast(riskFindPropertyMap)

  val riskData = tradeIdsRDD.mapPartitions{iter =>
    val mongoUri = mongoRslURIBr.value
    val mongoPropertyMap = propertyMapBr.value
    val snapshot = mongoPropertyMap("SNAPSHOT").toLong
    val mongoClient = new MongoClient(new MongoClientURI(mongoUri))
    val collection= mongoClient.getDatabase(mongoPropertyMap("HSBC")).getCollection(mongoPropertyMap("trades"))
    import scala.collection.JavaConverters._
    val doc= collection.find(in("riskSource.tradeId", iter.toList:_*))//, gt("validTo", snapshot), lt("validFrom", snapshot)))
    val itr= doc.iterator().asScala
    mongoClient.close()
    itr
  }
  riskData.foreach(x => {})
}
