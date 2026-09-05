package org.apache.spark.optima.api

import org.apache.spark.optima.listener.{OptimaExecutorStorageInfo, OptimaRDDStorageInfo, OptimaStore}
import org.apache.spark.internal.Logging
import org.apache.spark.status.AppStatusStore
import org.apache.spark.ui.{SparkUI, WebUIPage}
import org.json4s.{Extraction, JObject}

import javax.servlet.http.HttpServletRequest
import scala.xml.Node

class OptimaCachedStoragePage(ui: SparkUI, optimaStore: OptimaStore)
  extends WebUIPage("cachedstorage") with Logging {
  override def renderJson(request: HttpServletRequest) = {
    try {
      val liveRddStorage = ui.store.rddList()
      val rddStorage = optimaStore.rddStorageInfo()
      val graphs = ui.store.stageList(null)
        .filter(_.submissionTime.isDefined) // filter skipped or pending stages
        .map(stage => Tuple2(stage.stageId,
          ui.store.operationGraphForStage(stage.stageId).rootCluster.childClusters.flatMap(_.childNodes)
            .filter(_.cached)
            .map(rdd => {

              val liveCached = liveRddStorage.find(_.id == rdd.id).map(
                rdd => {
                  val maxUsageExecutor =  rdd.dataDistribution.map(executors => executors.maxBy(_.memoryUsed))
                  val maxExecutorUsage = maxUsageExecutor.map(executor =>
                    OptimaExecutorStorageInfo(
                      executor.memoryUsed,
                      executor.memoryRemaining,
                      if(executor.memoryUsed + executor.memoryRemaining != 0) executor.memoryUsed.toDouble / (executor.memoryUsed + executor.memoryRemaining) * 100 else 0
                  ))
                  OptimaRDDStorageInfo(rdd.id,
                                          rdd.memoryUsed,
                                          rdd.diskUsed,
                                          rdd.numPartitions,
                                          rdd.storageLevel,
                                          maxExecutorUsage
                )}
              )
              val cached = rddStorage.find(_.rddId == rdd.id)
              liveCached.getOrElse(cached)
            }))).toMap
      val jsonValue = Extraction.decompose(graphs)(org.json4s.DefaultFormats)
      jsonValue
    }
    catch {
      case e: Throwable => {
        logError("failed to serve optima Jobs RDD", e)
        JObject()
      }
    }
  }

  override def render(request: HttpServletRequest): Seq[Node] = Seq[Node]()
}
