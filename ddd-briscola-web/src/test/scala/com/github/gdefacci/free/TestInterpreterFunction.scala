package com.github.gdefacci.free

import scala.util.Try

trait InterpreterFunction {
  
  def apply[T](stp:ClientStep.Free[T]):Try[T]
  
}