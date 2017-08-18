package com.mongopoc.crud

import com.mongodb.{BasicDBObject, MongoClient, MongoClientURI, MongoConfigurationException}
import com.mongodb.spark.config.ReadConfig
import com.mongodb.spark.sql.toSparkSessionFunctions
import org.apache.spark.sql.Encoders
import org.bson.{BSON, Document}
import org.mongodb.scala.model.Filters._
import com.mongopoc.commons.{MongoConfigurations, SparkSessionProvider}
import com.mongopoc.commons.Constants._
import com.mongopoc.crud.LoadL3NestedJsonDataIntoMongoAtlas.{authSource, collection, mongoPassword, mongoSecurityEnabled, mongoUsername, mongo_host, replicaSet, ssl}
import org.apache.spark.broadcast.Broadcast


/**
  * Created by ajos21 on 8/17/2017.
  */
object JoinDslRiskTradeId extends SparkSessionProvider with MongoConfigurations{


  def riskDslTradeJoin()={
    val outputLoc: String= joinOutputLoc
    val bookIdList: List[String]= bookIds.toList
    val validTo: Long= validToDate
    val valifFrom: Long= validFromDate
    val propertyMap: Map[String, String] = createPropertyMap

    val mongoServers = mongo_host.replaceAll(",", ":" + mongo_port + ",") + ":" + mongo_port
    val mongoAtlasRiskURI = if (mongoSecurityEnabled)
      s"mongodb://$mongoUsername:$mongoPassword@$mongoServers/risk.$collection?ssl=$ssl&replicaSet=$replicaSet&authSource=$authSource"
    else
      s"mongodb://$mongoServers/risk.$collection?ssl=$ssl&replicaSet=$replicaSet"

    val mongoAtlasDslURI = if (mongoSecurityEnabled)
      s"mongodb://$mongoUsername:$mongoPassword@$mongoServers/risk.$dslCollection?ssl=$ssl&replicaSet=$replicaSet&authSource=$authSource"
    else
      s"mongodb://$mongoServers/risk.$dslCollection?ssl=$ssl&replicaSet=$replicaSet"

    //val dslURI = "mongodb://localhost:27017/dsl.measure_dsl"  //Enable For Local Testing and pass this to config
    val dslReadConfig = ReadConfig(Map(MONGO_URI -> mongoAtlasDslURI))

    import spark.implicits._
    val dslDFrame= spark.loadFromMongoDB(dslReadConfig)
    val propertyMap_Broadcast: Broadcast[Map[String, String]] = dslDFrame.rdd.context.broadcast(propertyMap)
    val encoder = Encoders.kryo[Document]
    val finalDf= dslDFrame.filter($"index.bookId".isin(bookIdList:_*)).select($"index.tradeId").distinct().mapPartitions{rowItr =>
      val propertyMapPartitionLevel = propertyMap_Broadcast.value
      val finalList: List[String]= rowItr.map{row=>
        row.getString(0)
      }.toList
      //connect To mongoDB
      //val mongoClient = new MongoClient( "localhost" , 27017 ) // for Local testing
      val mongoClient = new MongoClient( new MongoClientURI(mongoAtlasRiskURI))
      val collection= mongoClient.getDatabase(propertyMapPartitionLevel.get(DB_NAME).get).getCollection(propertyMapPartitionLevel.get(MONGO_COLLECTION).get)
      import scala.collection.JavaConverters._
      val doc= collection.find(and(in("riskSource.tradeId", finalList:_*), gt(VALID_TO, validTo), lt(VALID_FROM, valifFrom)))
      val itr= doc.iterator().asScala
      mongoClient.close()
      itr
    }(encoder).map{doc =>
      doc.toJson()
    }.write.json(outputLoc)
  }


  def createPropertyMap: Map[String, String] = {
    Map(DB_NAME -> mongoDbName , MONGO_COLLECTION -> collection)
  }



}
