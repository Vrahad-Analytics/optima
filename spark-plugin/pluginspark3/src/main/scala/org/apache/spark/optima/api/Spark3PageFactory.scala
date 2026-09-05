package org.apache.spark.optima.api

import org.apache.spark.optima.listener.OptimaStore
import org.apache.spark.sql.execution.ui.SQLAppStatusListener
import org.apache.spark.ui.{SparkUI, WebUIPage, WebUITab}

/**
 * Spark 3.x implementation of OptimaPageFactory using javax.servlet API
 */
class Spark3PageFactory extends OptimaPageFactory {
  
  override def createOptimaTab(ui: SparkUI): WebUITab = {
    new OptimaTab(ui)
  }
  
  override def createApplicationInfoPage(ui: SparkUI, optimaStore: OptimaStore): WebUIPage = {
    new OptimaApplicationInfoPage(ui, optimaStore)
  }
  
  override def createCachedStoragePage(ui: SparkUI, optimaStore: OptimaStore): WebUIPage = {
    new OptimaCachedStoragePage(ui, optimaStore)
  }
  
  override def createIcebergPage(ui: SparkUI, optimaStore: OptimaStore): WebUIPage = {
    new OptimaIcebergPage(ui, optimaStore)
  }
  
  override def createDeltaLakeScanPage(ui: SparkUI, optimaStore: OptimaStore): WebUIPage = {
    new OptimaDeltaLakeScanPage(ui, optimaStore)
  }
  
  override def createSQLMetricsPage(ui: SparkUI, sqlListener: () => Option[SQLAppStatusListener]): WebUIPage = {
    new OptimaSQLMetricsPage(ui, sqlListener)
  }
  
  override def createSQLPlanPage(ui: SparkUI, optimaStore: OptimaStore, sqlListener: () => Option[SQLAppStatusListener]): WebUIPage = {
    new OptimaSQLPlanPage(ui, optimaStore, sqlListener)
  }
  
  override def createSQLStagesRddPage(ui: SparkUI): WebUIPage = {
    new OptimaSQLStagesRddPage(ui)
  }
  
  override def addStaticHandler(ui: SparkUI, resourceBase: String, contextPath: String): Unit = {
    OptimaJettyUtils.addStaticHandler(ui, resourceBase, contextPath)
  }
  
  override def getTabs(ui: SparkUI): Seq[WebUITab] = {
    ui.getTabs.toSeq
  }
}
