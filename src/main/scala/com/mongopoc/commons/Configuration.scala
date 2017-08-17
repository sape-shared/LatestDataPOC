package com.mongopoc.commons

import java.io.File

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory._

/**
  * Created by sgar42 on 09-Aug-17.
  */
trait Configuration {
  implicit lazy val config: Config = {
    load("conf" + File.separator + "application.conf")
  }



}
