package org.obl.free

import org.scalatest.FunSuite

object ConsoleTestReporter {
  
  def apply[S](testResults:Seq[(Scenario[S], Seq[TestResult])]):Unit = {
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


trait ScalaTestReporter { self:FunSuite =>
  
  def verify[S](testResults:Seq[(Scenario[S], Seq[TestResult])]):Unit = {
    testResults.foreach { 
      case (scenario, steps) =>
        test(scenario.description) {
         steps.map {
            case Assert(v, desc) => 
              assert(v, desc)
              self.info.apply(desc,None)
            case Error(err) => fail(err)
          } 
        }
    }
  }
  
}