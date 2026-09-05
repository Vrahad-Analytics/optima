package org.apache.spark.optima.api

import org.apache.spark.optima.listener.OptimaStore
import org.apache.spark.internal.Logging
import org.apache.spark.ui.{SparkUI, WebUIPage}
import org.json4s.{Extraction, JObject}

import jakarta.servlet.http.HttpServletRequest
import scala.xml.Node

class OptimaApplicationInfoPage(ui: SparkUI, optimaStore: OptimaStore)
  extends WebUIPage("applicationinfo") with Logging {
  override def renderJson(request: HttpServletRequest) = {
    try {
      val applicationInfo = ui.store.applicationInfo()
      val environmentInfo = optimaStore.environmentInfo()
      val optimaApplicationInfo = OptimaApplicationInfo(None, applicationInfo, environmentInfo)
      val jsonValue = Extraction.decompose(optimaApplicationInfo)(org.json4s.DefaultFormats)
      jsonValue
    }
    catch {
      case e: Throwable => {
        logError("failed to serve optima application info", e)
        JObject()
      }
    }
  }

  override def render(request: HttpServletRequest): Seq[Node] = Seq[Node]()
}