package org.apache.spark.optima

import org.apache.spark.SparkContext
import org.apache.spark.optima.api.Spark4DatabricksPageFactory
import org.apache.spark.ui.SparkUI

/**
 * Databricks variant of the Spark 4 loader. Identical to the pluginspark4
 * loader except it instantiates `Spark4DatabricksPageFactory`, which
 * inverts the Databricks UI gate so the shaded jar serves UI only on DBR.
 * Same FQN as the upstream loader so the shared `SparkOptimaPlugin`
 * entrypoint resolves it without any per-flavor wiring.
 */
object OptimaSparkUILoader {
  private val pageFactory = new Spark4DatabricksPageFactory()

  def install(context: SparkContext): String =
    new org.apache.spark.optima.OptimaSparkUICommonInstaller().install(context, pageFactory)

  def loadUI(ui: SparkUI): String =
    new org.apache.spark.optima.OptimaSparkUICommonInstaller().loadUI(ui, pageFactory)
}
