package org.obl.brisola.free

object ConsoleTestReporter {
  
  def apply(testResults:Seq[(Scenario, Seq[TestResult])]):Unit = {
    testResults.foreach { 
      case (scenario, steps) =>
        println("-" * 120)
        println(scenario.description)
        println(steps.map {
          case Assert(false, desc) => "Error\n" + desc + "\n" + ("^" * 120)
          case Assert(true, desc) => desc
          case tr => "\n"+tr+ "\n"+("^" * 120)
        }.map(" - "+_).mkString("\n"))
    }
  }
  
}
