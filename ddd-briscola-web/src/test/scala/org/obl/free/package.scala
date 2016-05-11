package org.obl

package object free {
  
  type TestInterpreterFunction[S] = Step.Free[S,Any] => Seq[TestResult]
  
}