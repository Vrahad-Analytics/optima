package org.apache.spark.optima

import org.apache.spark.SparkContext
import org.apache.spark.optima.api.Spark3PageFactory
import org.apache.spark.ui.SparkUI

/**
 * Spark 3.x specific implementation of OptimaSparkUILoader that provides backward compatibility
 */
object OptimaSparkUILoader {
  
  private val pageFactory = new Spark3PageFactory()
  
  def install(context: SparkContext): String = {
    // Call the common implementation with Spark 3 factory
    new org.apache.spark.optima.OptimaSparkUICommonInstaller().install(context, pageFactory)
  }

  def loadUI(ui: SparkUI): String = {
    // Call the common implementation with Spark 3 factory
    new org.apache.spark.optima.OptimaSparkUICommonInstaller().loadUI(ui, pageFactory)
  }
}
