package org.apache.spark.optima.api

import org.apache.spark.optima.listener.OptimaStore
import org.apache.spark.sql.execution.ui.SQLAppStatusListener
import org.apache.spark.ui.{SparkUI, WebUIPage, WebUITab}

/**
 * Abstract factory for creating Optima UI components.
 * This allows version-specific implementations for different Spark versions.
 */
abstract class OptimaPageFactory {
  
  def createOptimaTab(ui: SparkUI): WebUITab
  
  def createApplicationInfoPage(ui: SparkUI, optimaStore: OptimaStore): WebUIPage
  
  def createCachedStoragePage(ui: SparkUI, optimaStore: OptimaStore): WebUIPage
  
  def createIcebergPage(ui: SparkUI, optimaStore: OptimaStore): WebUIPage
  
  def createDeltaLakeScanPage(ui: SparkUI, optimaStore: OptimaStore): WebUIPage
  
  def createSQLMetricsPage(ui: SparkUI, sqlListener: () => Option[SQLAppStatusListener]): WebUIPage
  
  def createSQLPlanPage(ui: SparkUI, optimaStore: OptimaStore, sqlListener: () => Option[SQLAppStatusListener]): WebUIPage
  
  def createSQLStagesRddPage(ui: SparkUI): WebUIPage

  def addStaticHandler(ui: SparkUI, resourceBase: String, contextPath: String): Unit

  def getTabs(ui: SparkUI): Seq[WebUITab]

  def isUISupported(ui: SparkUI): Boolean = true
}
