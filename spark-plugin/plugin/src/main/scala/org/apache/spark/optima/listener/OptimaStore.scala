package org.apache.spark.optima.listener

import scala.collection.JavaConverters._
import org.apache.spark.util.Utils
import org.apache.spark.util.kvstore.{KVStore, KVStoreView}


class OptimaStore(val store: KVStore) {
  // mapToSeq copied from KVUtils because it does not exists in spark 3.3
  def mapToSeq[T, B](view: KVStoreView[T])(mapFunc: T => B): Seq[B] = {
    Utils.tryWithResource(view.closeableIterator()) { iter =>
      iter.asScala.map(mapFunc).toList
    }
  }

  def icebergCommits(offset: Int, length: Int): Seq[IcebergCommitInfo] = {
    mapToSeq(store.view(classOf[IcebergCommitWrapper]))(_.info).filter(_.executionId >= offset).take(length).sortBy(_.executionId)
  }

  def databricksAdditionalExecutionInfo(offset: Int, length: Int): Seq[DatabricksAdditionalExecutionInfo] = {
    mapToSeq(store.view(classOf[DatabricksAdditionalExecutionWrapper]))(_.info).filter(_.executionId >= offset).take(length).sortBy(_.executionId)
  }

  def environmentInfo(): Option[OptimaEnvironmentInfo] = {
    mapToSeq(store.view(classOf[OptimaEnvironmentInfoWrapper]))(_.info).headOption
  }

  def rddStorageInfo(): Seq[OptimaRDDStorageInfo] = {
    mapToSeq(store.view(classOf[OptimaRDDStorageInfoWrapper]))(_.info)
  }

  def deltaLakeScanInfo(offset: Int, length: Int): Seq[OptimaDeltaLakeScanInfo] = {
    mapToSeq(store.view(classOf[OptimaDeltaLakeScanInfoWrapper]))(_.info)
      .filter(_.minExecutionId >= offset)
      .take(length)
      .sortBy(_.minExecutionId)
  }

}
