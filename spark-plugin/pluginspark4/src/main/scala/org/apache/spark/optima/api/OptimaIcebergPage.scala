package org.apache.spark.optima.api

import org.apache.spark.optima.listener.OptimaStore
import org.apache.spark.internal.Logging
import org.apache.spark.ui.{SparkUI, WebUIPage}
import org.json4s.{Extraction, JObject}

import jakarta.servlet.http.HttpServletRequest
import scala.xml.Node

class OptimaIcebergPage(ui: SparkUI, optimaStore: OptimaStore)
  extends WebUIPage("iceberg") with Logging {
  override def renderJson(request: HttpServletRequest) = {
    try {
      val offset = request.getParameter("offset")
      val length = request.getParameter("length")
      if (offset == null || length == null) {
        JObject()
      } else {
        val commits = optimaStore.icebergCommits(offset.toInt, length.toInt)
        val icebergInfo = IcebergInfo(commitsInfo = commits)
        val jsonValue = Extraction.decompose(icebergInfo)(org.json4s.DefaultFormats)
        jsonValue
      }
    }
    catch {
      case e: Throwable => {
        logError("failed to serve optima iceberg", e)
        JObject()
      }
    }
  }

  override def render(request: HttpServletRequest): Seq[Node] = Seq[Node]()
}
