package org.apache.spark.optima.api

import org.apache.spark.optima.listener.OptimaStore
import org.apache.spark.sql.execution.ui.SQLAppStatusListener
import org.apache.spark.ui.{SparkUI, WebUIPage, WebUITab}

/**
 * Spark 4.x implementation of OptimaPageFactory using jakarta.servlet API
 */
class Spark4PageFactory extends OptimaPageFactory {
  
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

  // Databricks Runtime 17.3 (Spark 4 based) ships javax.servlet instead of jakarta.servlet,
  // so any access to jakarta.servlet.* in this module crashes with NoClassDefFoundError.
  // Skip the entire Optima UI on Databricks; listeners (data export) still run.
  override def isUISupported(ui: SparkUI): Boolean = {
    !ui.conf.getOption("spark.databricks.clusterUsageTags.cloudProvider").isDefined
  }
}
