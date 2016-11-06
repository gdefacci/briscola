package com.github.gdefacci.briscola

import com.github.gdefacci.bdd._
import com.github.gdefacci.bdd.testkit.FeaturesRunner
import com.github.gdefacci.bdd.testkit.TestInfos

object PlayersFeatures extends PlayerSteps with Features {
  
  lazy val `can create a player` = scenario(`given an initial application state` 
      Then can(`create player`("name", "password")))

   lazy val `can create 3 players` = scenario(`given an initial application state` 
      Then can(`create player`("name", "password"))
      And can(`create player`("name1", "password"))
      And can(`create player`("name2", "password")))
   
      
  lazy val `cant create player if one with same name already exists` = scenario(
      `given an initial application state`
      And `is created player`("name", "password")
      Then cant(`create player`("name", "password")))
      
  lazy val features = new Feature("Player Creation Feature", 
      `can create a player`, 
      `can create 3 players`,
      `cant create player if one with same name already exists`) :: Nil     

}