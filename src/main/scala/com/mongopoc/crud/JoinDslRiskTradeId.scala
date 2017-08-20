package com.mongopoc.crud

import com.mongodb.{ MongoClient, MongoClientURI}
import com.mongodb.spark.config.ReadConfig
import com.mongodb.spark.sql.toSparkSessionFunctions
import org.apache.spark.sql.Encoders
import org.bson.{Document}
import org.mongodb.scala.model.Filters._
import com.mongopoc.commons.{MongoConfigurations, SparkSessionProvider}
import com.mongopoc.commons.Constants._
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
    val propertyMap: Map[String, String] = Map(DB_NAME -> mongoDbName , MONGO_COLLECTION -> collection)

    val mongoServers = mongo_host.replaceAll(",", ":" + mongo_port + ",") + ":" + mongo_port
    val mongoAtlasRiskURI = createRiskURI(mongoServers)

    val mongoAtlasDslURI = createDslURI(mongoServers)

    val dslReadConfig = ReadConfig(Map(MONGO_URI -> mongoAtlasDslURI))

    import spark.implicits._
    lazy val dslDFrame= spark.loadFromMongoDB(dslReadConfig)
    val propertyMap_Broadcast: Broadcast[Map[String, String]] = dslDFrame.rdd.context.broadcast(propertyMap)
    val encoder = Encoders.kryo[Document]
    val noOfPartition= excecutors
    val finalDf= dslDFrame.filter($"index.bookId".isin(bookIdList:_*)).select($"index.tradeId").distinct().repartition(noOfPartition).mapPartitions{rowItr =>
      val propertyMapPartitionLevel = propertyMap_Broadcast.value
      val finalList: List[String]= rowItr.map{row=>
        row.getString(0)
      }.toList
      //connect To mongoDB
      val mongoClient = new MongoClient( new MongoClientURI(mongoAtlasRiskURI))
      val collection= mongoClient.getDatabase(propertyMapPartitionLevel.get(DB_NAME).get).getCollection(propertyMapPartitionLevel.get(MONGO_COLLECTION).get)
      import scala.collection.JavaConverters._
      val doc= collection.find(and(in("riskSource.tradeId", finalList:_*), gt(VALID_TO, validTo), lt(VALID_FROM, valifFrom)))
      val itr= doc.iterator().asScala
      mongoClient.close()
      itr
    }(encoder).map{doc =>
      doc.toJson()
    }.write.text(outputLoc)
  }


 def createRiskURI(mongoServers: String)= {
   if(localTest)
     s"mongodb://$mongoServers/risk.$collection"
   else if (mongoSecurityEnabled)
     s"mongodb://$mongoUsername:$mongoPassword@$mongoServers/risk.$collection?ssl=$ssl&replicaSet=$replicaSet&authSource=$authSource"
   else
     s"mongodb://$mongoServers/risk.$collection?ssl=$ssl&replicaSet=$replicaSet"
 }

  def createDslURI(mongoServers: String)= {
    if(localTest)
      s"mongodb://$mongoServers/dsl.$dslCollection"
    else if (mongoSecurityEnabled)
      s"mongodb://$mongoUsername:$mongoPassword@$mongoServers/risk.$dslCollection?ssl=$ssl&replicaSet=$replicaSet&authSource=$authSource"
    else
      s"mongodb://$mongoServers/dsl.$dslCollection?ssl=$ssl&replicaSet=$replicaSet"
  }


}
