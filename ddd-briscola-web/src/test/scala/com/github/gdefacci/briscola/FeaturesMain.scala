package com.github.gdefacci.briscola

import com.github.gdefacci.bdd.testkit._

object FeaturesMain extends App {
  
  
  val tests = new TestInfos(FeaturesRunner.run(GameFeatures))
  
  println(tests.failedScenarios.map(Descriptions.failedScenario(_)).mkString("\n\n"))
  
}