package org.obl.brisola.free

case class Scenario(description: String, step: Step.FreeStep[Any])

trait ScenariosRunner {
  
  def scenarios:Seq[Scenario]
  
  def runner:Step.FreeStep[Any] => Seq[TestResult]
  
  lazy val testResults = scenarios.map { scenario =>
    println(s"executing ${scenario.description}")
    scenario -> runner(scenario.step)
  }
  
}