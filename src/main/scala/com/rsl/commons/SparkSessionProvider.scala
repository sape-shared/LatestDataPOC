package com.rsl.commons

import org.apache.spark.sql.SparkSession

/**
  * Created by sgar42 on 04-Aug-17.
  */
class SparkSessionProvider {
  val spark = SparkSession.builder()
    .master("local[*]")
    .appName("LatestDataPOC")
    .getOrCreate()
}
