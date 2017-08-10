package com.mongopoc.crud

import com.mongopoc.crud.LoadL3NestedJsonDataIntoMongoAtlas._

object InsertAndUpdateL3DataIntoMongoAtlas extends App {
  val (base_input_location, start_date, num_days) = (args(0), args(1), args(2))

  loadL3DataIntoMongoAtlas(base_input_location, start_date, num_days.toInt, true)
}
