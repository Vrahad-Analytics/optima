package org.apache.spark.optima

import org.apache.spark.SparkContext
import org.apache.spark.optima.api.OptimaPageFactory
import org.apache.spark.optima.listener.{OptimaDatabricksLiveListener, OptimaEnvironmentInfo, OptimaEnvironmentInfoEvent, OptimaListener, OptimaStore, DeltaLakeInstrumentationListener}
import org.apache.spark.optima.iceberg.ClassLoaderChecker
import org.apache.spark.optima.iceberg.ClassLoaderChecker.isMetricLoaderInRightClassLoader
import org.apache.spark.internal.Logging
import org.apache.spark.scheduler.SparkListenerInterface
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.ui.SQLAppStatusListener
import org.apache.spark.status.{ElementTrackingStore, LiveRDDsListener}
import org.apache.spark.ui.SparkUI

class OptimaSparkUICommonInstaller extends Logging {
  def install(context: SparkContext, pageFactory: OptimaPageFactory): String = {
    if(context.ui.isEmpty) {
      logWarning("No UI detected, skipping installation...")
      return ""
    }
    val isOptimaAlreadyInstalled = pageFactory.getTabs(context.ui.get).exists(_.name == "Optima")
    if(isOptimaAlreadyInstalled){
      logInfo("Optima UI is already installed, skipping installation...")
      return context.ui.get.webUrl
    }

    val optimaEnabled = context.conf.getBoolean("spark.optima.enabled", true)
    if (!optimaEnabled) {
      logInfo("Optima is disabled via spark.optima.enabled, skipping installation...")
      return context.ui.get.webUrl
    }

    val sqlListener = () => context.listenerBus.listeners.toArray().find(_.isInstanceOf[SQLAppStatusListener]).asInstanceOf[Option[SQLAppStatusListener]]
    val optimaListener = new OptimaListener(context.statusStore.store.asInstanceOf[ElementTrackingStore])

    val runtime = Runtime.getRuntime
    val driverXmxBytes = runtime.maxMemory()
    val environmentInfo = OptimaEnvironmentInfo(driverXmxBytes)
    val isDatabricks = context.conf.getOption("spark.databricks.clusterUsageTags.cloudProvider").isDefined
    val icebergInstalled = context.conf.get("spark.sql.extensions", "").contains("org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
    val icebergEnabled = context.conf.getBoolean("spark.optima.iceberg.enabled", defaultValue = true)
    val cacheObservabilityEnabled = context.conf.getBoolean("spark.optima.cacheObservability.enabled", defaultValue = true)
    val deltaLakeInstrumentationEnabled = context.conf.getBoolean("spark.optima.instrument.deltalake.enabled", defaultValue = false)
    val deltaLakeCollectZindexFields = context.conf.getBoolean("spark.optima.instrument.deltalake.collectZindexFields", defaultValue = false)
    val deltaLakeCacheZindexFieldsToProperties = context.conf.getBoolean("spark.optima.instrument.deltalake.cacheZindexFieldsToProperties", defaultValue = true)
    val deltaLakeHistoryLimit = context.conf.getInt("spark.optima.instrument.deltalake.historyLimit", defaultValue = 1000)
    val icebergAuthCatalogDiscovery = context.conf.getBoolean("spark.optima.iceberg.autoCatalogDiscovery", defaultValue = false)
    if(icebergInstalled && icebergEnabled) {
      if(icebergAuthCatalogDiscovery && isMetricLoaderInRightClassLoader()) {
        context.conf.getAll.filter(_._1.startsWith("spark.sql.catalog")).filter(keyValue => keyValue._2 == "org.apache.iceberg.spark.SparkCatalog" || keyValue._2 == "org.apache.iceberg.spark.SparkSessionCatalog").foreach(keyValue => {
          val configName = s"${keyValue._1}.metrics-reporter-impl"
          context.conf.getOption(configName) match {
            case Some(currentConfig) => {
              if(currentConfig == "org.apache.spark.optima.iceberg.OptimaIcebergMetricsReporter") {
                logInfo(s"Metric reporter already exist in config: ${configName}, no need to set it with optima iceberg auto discovery")
              } else {
                logWarning(s"Different metric reporter already exist in config: ${configName}, cannot set metric reporter to OptimaIcebergMetricsReporter")
              }
            }
            case None => {
              if(icebergAuthCatalogDiscovery) {
                context.conf.set(configName, "org.apache.spark.optima.iceberg.OptimaIcebergMetricsReporter")
                logInfo(s"set ${configName} reporter to OptimaIcebergMetricsReporter")
              } else {
                logWarning(s"OptimaIcebergMetricsReporter is missing for iceberg catalog ${configName}, for optima iceberg observability set spark.optima.iceberg.autoCatalogDiscovery to true or set the metric reporter manually to org.apache.spark.optima.iceberg.OptimaIcebergMetricsReporter")
              }
            }
          }
        })
      }
    }
    try {
      val addToQueueMethod =
        if (isDatabricks) (listener: SparkListenerInterface, queue: String) =>
          context.listenerBus.getClass.getMethods.find(_.getName == "addToQueue").head.invoke(context.listenerBus, listener, queue, None)
            .asInstanceOf[Unit]
        else (listener: SparkListenerInterface, queue: String) => context.listenerBus.addToQueue(listener, queue)
      addToQueueMethod(optimaListener, "optima")

      if(cacheObservabilityEnabled) {
        val rddListener = new LiveRDDsListener(context.statusStore.store.asInstanceOf[ElementTrackingStore])
        addToQueueMethod(rddListener, "optima")
      }
      if(deltaLakeInstrumentationEnabled) {
        val deltaLakeListener = new DeltaLakeInstrumentationListener(context, deltaLakeCollectZindexFields, deltaLakeCacheZindexFieldsToProperties, deltaLakeHistoryLimit, isDatabricks)
        addToQueueMethod(deltaLakeListener, "optima")
        logInfo("Added DeltaLakeInstrumentationListener to the listener bus")
      }
      context.listenerBus.post(OptimaEnvironmentInfoEvent(environmentInfo))
      if (isDatabricks) {
        addToQueueMethod(OptimaDatabricksLiveListener(context.listenerBus), "optima")
      }
    } catch {
      case e: Throwable =>
        logWarning("Could not add Optima Listeners to listener bus", e)
    }

    loadUI(context.ui.get, pageFactory, sqlListener)
  }

  def loadUI(ui: SparkUI, pageFactory: OptimaPageFactory, sqlListener: () => Option[SQLAppStatusListener] = () => None): String = {
    val isOptimaAlreadyInstalled = pageFactory.getTabs(ui).exists(_.name == "Optima")
    if (isOptimaAlreadyInstalled) {
      logInfo("Optima UI is already installed, skipping installation...")
      return ui.webUrl
    }
    if (!pageFactory.isUISupported(ui)) {
      logWarning("Optima UI is not supported in this environment, skipping UI installation; listeners will still run")
      return ui.webUrl
    }
    pageFactory.addStaticHandler(ui, "io/telemetria/optima/static/ui", ui.basePath + "/optima")
    val optimaStore = new OptimaStore(store = ui.store.store)
    val tab = pageFactory.createOptimaTab(ui)
    tab.attachPage(pageFactory.createSQLPlanPage(ui, optimaStore, sqlListener))
    tab.attachPage(pageFactory.createSQLMetricsPage(ui, sqlListener))
    tab.attachPage(pageFactory.createSQLStagesRddPage(ui))
    tab.attachPage(pageFactory.createApplicationInfoPage(ui, optimaStore))
    tab.attachPage(pageFactory.createIcebergPage(ui, optimaStore))
    tab.attachPage(pageFactory.createDeltaLakeScanPage(ui, optimaStore))
    tab.attachPage(pageFactory.createCachedStoragePage(ui, optimaStore))
    ui.attachTab(tab)
    ui.webUrl
  }

}

object OptimaSparkUICommonLoader extends Logging {

  private val OPTIMA_EXTENSION_CLASS = "org.apache.spark.optima.OptimaInstrumentationExtension"
  val INSTRUMENT_SPARK_ENABLED = "spark.optima.instrument.spark.enabled"
  val INSTRUMENT_MAP_IN_PANDAS_ENABLED = "spark.optima.instrument.spark.mapInPandas.enabled"
  val INSTRUMENT_MAP_IN_ARROW_ENABLED = "spark.optima.instrument.spark.mapInArrow.enabled"
  val INSTRUMENT_WINDOW_ENABLED = "spark.optima.instrument.spark.window.enabled"
  val INSTRUMENT_ARROW_EVAL_PYTHON_ENABLED = "spark.optima.instrument.spark.arrowEvalPython.enabled"
  val INSTRUMENT_BATCH_EVAL_PYTHON_ENABLED = "spark.optima.instrument.spark.batchEvalPython.enabled"
  val INSTRUMENT_FLAT_MAP_GROUPS_PANDAS_ENABLED = "spark.optima.instrument.spark.flatMapGroupsInPandas.enabled"
  val INSTRUMENT_FLAT_MAP_COGROUPS_PANDAS_ENABLED = "spark.optima.instrument.spark.flatMapCoGroupsInPandas.enabled"
  val INSTRUMENT_SQL_NODES_ENABLED = "spark.optima.instrument.spark.sqlNodes.enabled"

  def install(context: SparkContext, pageFactory: OptimaPageFactory): String = {
    new OptimaSparkUICommonInstaller().install(context, pageFactory)
  }

  def loadUI(ui: SparkUI, pageFactory: OptimaPageFactory): String = {
    new OptimaSparkUICommonInstaller().loadUI(ui, pageFactory)
  }
  
  // Backward compatibility methods - these will be overridden in version-specific implementations
  def install(context: SparkContext): String = {
    throw new UnsupportedOperationException("This method requires a version-specific implementation. Use pluginspark3 or pluginspark4.")
  }

  def loadUI(ui: SparkUI): String = {
    throw new UnsupportedOperationException("This method requires a version-specific implementation. Use pluginspark3 or pluginspark4.")
  }

  /**
   * Registers the Optima instrumentation extension in spark.sql.extensions if not already present.
   * This must be called during plugin init() because spark.sql.extensions is read when SparkSession
   * is created, which occurs after plugin initialization.
   *
   * This method is in the org.apache.spark.optima package to access SparkContext.conf
   * (which is private[spark]).
   */
  def registerInstrumentationExtension(sc: SparkContext): Unit = {
    val instrumentEnabled = sc.conf.getBoolean(INSTRUMENT_SPARK_ENABLED, defaultValue = false)
    val mapInPandasEnabled = sc.conf.getBoolean(INSTRUMENT_MAP_IN_PANDAS_ENABLED, defaultValue = false)
    val mapInArrowEnabled = sc.conf.getBoolean(INSTRUMENT_MAP_IN_ARROW_ENABLED, defaultValue = false)
    val windowEnabled = sc.conf.getBoolean(INSTRUMENT_WINDOW_ENABLED, defaultValue = false)
    val arrowEvalPythonEnabled = sc.conf.getBoolean(INSTRUMENT_ARROW_EVAL_PYTHON_ENABLED, defaultValue = false)
    val batchEvalPythonEnabled = sc.conf.getBoolean(INSTRUMENT_BATCH_EVAL_PYTHON_ENABLED, defaultValue = false)
    val flatMapGroupsPandasEnabled = sc.conf.getBoolean(INSTRUMENT_FLAT_MAP_GROUPS_PANDAS_ENABLED, defaultValue = false)
    val flatMapCogroupsPandasEnabled = sc.conf.getBoolean(INSTRUMENT_FLAT_MAP_COGROUPS_PANDAS_ENABLED, defaultValue = false)
    val anyInstrumentationEnabled = instrumentEnabled || mapInPandasEnabled || mapInArrowEnabled ||
      windowEnabled || arrowEvalPythonEnabled || batchEvalPythonEnabled ||
      flatMapGroupsPandasEnabled || flatMapCogroupsPandasEnabled
    if (!anyInstrumentationEnabled) {
      logInfo("Optima instrumentation extension is disabled (no instrumentation flags enabled)")
      return
    }

    try {
      val currentExtensions = sc.conf.get("spark.sql.extensions", "")
      if (currentExtensions.contains(OPTIMA_EXTENSION_CLASS)) {
        logInfo("Optima instrumentation extension is already registered in spark.sql.extensions")
      } else {
        val newExtensions = if (currentExtensions.isEmpty) {
          OPTIMA_EXTENSION_CLASS
        } else {
          s"$currentExtensions,$OPTIMA_EXTENSION_CLASS"
        }
        sc.conf.set("spark.sql.extensions", newExtensions)
        logInfo(s"Registered Optima instrumentation extension in spark.sql.extensions")
      }
    } catch {
      case e: Throwable =>
        logWarning("Could not register Optima instrumentation extension", e)
    }
  }
}
