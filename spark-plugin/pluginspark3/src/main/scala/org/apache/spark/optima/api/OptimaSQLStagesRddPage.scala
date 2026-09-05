package org.apache.spark.optima.api

import org.apache.spark.internal.Logging
import org.apache.spark.ui.{SparkUI, WebUIPage}
import org.json4s.{Extraction, JObject}

import javax.servlet.http.HttpServletRequest
import scala.xml.Node

class OptimaSQLStagesRddPage(ui: SparkUI)
  extends WebUIPage("stagesrdd") with Logging {
  override def renderJson(request: HttpServletRequest) = {
    try {
      val graphs = ui.store.stageList(null)
        .filter(_.submissionTime.isDefined) // filter skipped or pending stages
        .map(stage => Tuple2(stage.stageId,
          ui.store.operationGraphForStage(stage.stageId).rootCluster.childClusters
            .map(rdd => Tuple2(rdd.id, rdd.name)).toMap))
        .toMap
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
