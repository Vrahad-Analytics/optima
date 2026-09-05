package io.telemetria.optima

import org.apache.spark.SparkContext
import org.apache.spark.optima.{OptimaSparkUICommonLoader, OptimaSparkUILoader}

object SparkOptima {
  def install(context: SparkContext): Unit = {
      OptimaSparkUILoader.install(context)
  }
}
