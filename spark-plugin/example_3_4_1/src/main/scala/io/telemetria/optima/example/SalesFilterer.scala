package io.telemetria.optima.example

import org.apache.spark.sql.{SaveMode, SparkSession}

import java.nio.file.Paths

object SalesFilterer extends App {
  val spark = SparkSession
    .builder()
    .appName("Sales Filterer")
    .config("spark.plugins", "io.telemetria.optima.SparkOptimaPlugin")
    .config("spark.ui.port", "10000")
    .config("spark.eventLog.enabled", true)
    .config("spark.sql.maxMetadataStringLength", "10000")
    .master("local[1]")
    .getOrCreate()

  import spark.implicits._

  spark.read
    .load("/Users/menishmueli/Documents/GitHub/spark-sql-perf/data/store_sales")
    .filter($"ss_quantity" > 1)
    .write
    .mode(SaveMode.Overwrite)
    .partitionBy("ss_quantity")
    .parquet("/tmp/store_sales")

  // scala.io.StdIn.readLine()
  spark.stop()
}
