package io.telemetria.optima.example

import org.apache.spark.sql.SparkSession

object SchedulingSmallTasksSkipAlerts extends App {
  val spark = SparkSession
    .builder()
    .appName("SchedulingSmallTasks")
    .config("spark.plugins", "io.telemetria.optima.SparkOptimaPlugin")
    .config("spark.ui.port", "10000")
    .config("spark.sql.maxMetadataStringLength", "10000")
    .config("spark.optima.alert.disabled", "smallTasks,idleCoresTooHigh")
    .master("local[*]")
    .getOrCreate()

  val numbers = spark.range(0, 10000).repartition(10000).count()

  println(s"count numbers to 10000: $numbers")

  scala.io.StdIn.readLine()
  spark.stop()
}
