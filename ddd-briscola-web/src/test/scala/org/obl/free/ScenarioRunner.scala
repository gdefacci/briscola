package org.obl.free

import scalaz.Free

case class Scenario[S](description: String, step:Step.Free[S, Any])

trait ScenariosRunner[S] {
  
  def scenarios:Seq[Scenario[S]]
  
  def runner:Step.Free[S, Any] => Seq[TestResult]
  
  lazy val testResults = scenarios.map { scenario =>
    scenario -> runner(scenario.step)
  }
  
}