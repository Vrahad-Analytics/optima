package io.telemetria.optima

import org.apache.spark.SparkContext
import org.apache.spark.api.plugin.{DriverPlugin, ExecutorPlugin, PluginContext, SparkPlugin}
import org.apache.spark.optima.{OptimaSparkUICommonLoader, OptimaSparkUILoader}
import org.apache.spark.internal.Logging

import java.util
import scala.collection.JavaConverters.mapAsJavaMapConverter

class SparkOptimaPlugin extends SparkPlugin {
  override def driverPlugin(): DriverPlugin = new SparkOptimaDriverPlugin()

  override def executorPlugin(): ExecutorPlugin = null
}

class SparkOptimaDriverPlugin extends DriverPlugin with Logging {
  var sc: SparkContext = null

  override def init(sc: SparkContext, pluginContext: PluginContext): util.Map[String, String] = {
    this.sc = sc
    OptimaSparkUICommonLoader.registerInstrumentationExtension(sc)
    Map[String, String]().asJava
  }

  override def registerMetrics(appId: String, pluginContext: PluginContext): Unit = {
    var webUrl = OptimaSparkUILoader.install(sc)
    logInfo(s"spark optima url is $webUrl/optima")
    super.registerMetrics(appId, pluginContext)
  }
}
