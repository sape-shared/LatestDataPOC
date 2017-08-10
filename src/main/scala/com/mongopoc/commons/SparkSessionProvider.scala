package com.mongopoc.commons

import java.util.Map.Entry

import com.typesafe.config.ConfigValue
import org.apache.spark.sql.SparkSession

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by sgar42 on 04-Aug-17.
  */
trait SparkSessionProvider extends Configuration {

  // val sparkConfSettings: mutable.Set[(String, ConfigValue)] = config.getConfig("configuration.spark").entrySet().asScala.map(entry => (entry.getKey, entry.getValue))

  val spark = SparkSession.builder()
    .master(config.getString("configuration.spark.master.url"))
    .appName("Bulk Ingestion Mongo POC")
    .getOrCreate()
}
