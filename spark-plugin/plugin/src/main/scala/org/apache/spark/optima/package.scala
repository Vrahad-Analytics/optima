package org.apache.spark

package object optima {
  private[optima] type EnumValue[A <: Enumeration] = A#Value
}
