package com.github.gdefacci.briscola

import com.github.gdefacci.bdd.Feature
import com.github.gdefacci.bdd.Features

object GameFeatures extends GameSteps with Features {

  val `given a game with pippo pluto and minni is started` = (
    `given an initial application state`
    And `given initial players`(List("pippo", "pluto", "minni"))
    And `player starts match`("pippo", List("pluto", "minni"))
    And `players accept the competition starting the game`(List("pluto", "minni")))

  val `can play a card` = scenario(
    `given a game with pippo pluto and minni is started`
      And can(`current player play`(`a random card`)))

  val `can play a turn` = scenario(
    `given a game with pippo pluto and minni is started`
      And `current player play`(`a random card`)
      And `current player play`(`a random card`)
      And can(`current player play`(`a random card`)))

  val features = new Feature("game features", 
      `can play a card`, 
      `can play a turn`) :: Nil

}