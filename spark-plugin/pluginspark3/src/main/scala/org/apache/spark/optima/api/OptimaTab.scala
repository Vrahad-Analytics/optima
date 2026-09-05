package org.apache.spark.optima.api

import org.apache.spark.ui.{SparkUI, UIUtils, WebUITab}

import javax.servlet.http.HttpServletRequest
import scala.xml.Node

class OptimaTab(parent: SparkUI) extends WebUITab(parent,"optima") {
  override val name: String = "Optima"
  def render(request: HttpServletRequest): Seq[Node] = {
    val content =
          <div>
          </div>
    UIUtils.basicSparkPage(request, content, "Optima", true)
  }
}
