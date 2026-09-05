package org.apache.spark.optima.iceberg

import org.apache.iceberg.CatalogUtil
import org.apache.spark.internal.Logging

object ClassLoaderChecker extends Logging {
  def isMetricLoaderInRightClassLoader(): Boolean = {
    val metricReporterClass = classOf[OptimaIcebergMetricsReporter]
    val classLoaderMetricReporter = metricReporterClass.getClassLoader.toString
    val classLoaderIcebergCatalog = classOf[CatalogUtil].getClassLoader.toString
    try {
      Class.forName(metricReporterClass.getCanonicalName, false, classOf[CatalogUtil].getClassLoader)
    }
    catch {
      case _: NoClassDefFoundError =>
        logWarning(s"Cannot load iceberg MetricsReporter from optima classloader, which prevents optima iceberg observability support. iceberg classloader: ${classOf[CatalogUtil].getClassLoader.toString}")
        return false
      case _: ClassNotFoundException =>
        logWarning(s"Cannot load OptimaIcebergMetricsReporter from iceberg classloader, which prevents optima iceberg observability support. iceberg classloader: ${classLoaderIcebergCatalog} metric reporter classloader: ${classLoaderMetricReporter}")
        return false
      case error: Throwable =>
        logError(s"Unexpected error while trying to load, can use OptimaIcebergMetricsReporter. iceberg classloader: ${classLoaderIcebergCatalog} metric reporter classloader: ${classLoaderMetricReporter}", error)
        return false
    }
    true
  }
}
