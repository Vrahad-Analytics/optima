package org.apache.spark.deploy.history

import org.apache.spark.SparkConf
import org.apache.spark.optima.OptimaSparkUILoader
import org.apache.spark.optima.listener.OptimaListener
import org.apache.spark.scheduler.SparkListener
import org.apache.spark.status.{AppHistoryServerPlugin, ElementTrackingStore, LiveRDDsListener}
import org.apache.spark.ui.SparkUI

class OptimaHistoryServerPlugin extends AppHistoryServerPlugin {

  override def createListeners(conf: SparkConf, store: ElementTrackingStore): Seq[SparkListener] = {
    Seq(new OptimaListener(store), new LiveRDDsListener(store))
  }

  override def setupUI(ui: SparkUI): Unit = {
    OptimaSparkUILoader.loadUI(ui)
  }
}