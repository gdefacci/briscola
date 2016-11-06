package com.github.gdefacci.briscola

import com.github.gdefacci.bdd.Features
import com.github.gdefacci.bdd.Feature

object CompetitionFeatures extends Features with CompetitionSteps {

  lazy val `a player can't start a competition with (her/his)self` = scenario(
    `given an initial application state`
      And `create player`("name", "password")
      Then cant(`player starts match`("name", Seq("name"))))

  lazy val `a player can start a competition` = scenario(
    `given an initial application state`
      And `create player`("name", "password")
      And `create player`("name1", "password")
      Then can(`player starts match`("name", Seq("name1")))
      And `player received message`("name1", `a valid created competition event`))

  lazy val `a player can accept a competition` = scenario(
    `given an initial application state`
      And `given initial players`(List("name", "name1", "name2"))
      Then `player starts match`("name", Seq("name1", "name2"))
      And `player accept the competition`("name1")
      And `player received message`("name", `an accepted competition event`))

  lazy val `when every one has accepted the competition the game starts` = scenario(
    `given an initial application state`
      And `given initial players`(List("name", "name1"))
      And `player starts match`("name", Seq("name1"))
      Then can(`player accept the competition`("name1"))
      And `all players received message`(`a game started event`) )

  lazy val `a player can't accept an already accepted competition` = scenario(
    `given an initial application state`
      And `given initial players`(List("name", "name1", "name2"))
      Then `player starts match`("name", Seq("name1", "name2"))
      And `player accept the competition`("name1")
      But cant(`player accept the competition`("name1")))

  lazy val `a player can decline a competition` = scenario(
    `given an initial application state`
      And `given initial players`(List("name", "name1", "name2"))
      Then `player starts match`("name", Seq("name1", "name2"))
      And can(`player decline the competition`("name1"))
      And `player received message`("name", `a declined competition event`))

  lazy val `a player can't decline an already declined competition` = scenario(
    `given an initial application state`
      And `given initial players`(List("name", "name1", "name2"))
      Then `player starts match`("name", Seq("name1", "name2"))
      And can(`player decline the competition`("name1"))
      And cant(`player decline the competition`("name1")))

  lazy val `a player can decline and later accept a competition` = scenario(
    `given an initial application state`
      And `given initial players`(List("name", "name1", "name2"))
      Then `player starts a "on player count" match`("name", Seq("name1", "name2"), playerCount = 2)
      And can(`player decline the competition`("name1"))
      And can(`player accept the competition`("name1")))

  lazy val `a player can accept and later decline a competition` = scenario(
    `given an initial application state`
      And `given initial players`(List("name", "name1", "name2"))
      Then `player starts a "on player count" match`("name", Seq("name1", "name2"), playerCount = 2)
      And can(`player decline the competition`("name1"))
      And can(`player accept the competition`("name1")))

  lazy val features = new Feature("game features",
    `a player can start a competition`,
    `a player can't start a competition with (her/his)self`,
    `a player can accept a competition`,
    `when every one has accepted the competition the game starts`,
    `a player can decline a competition`,
    `a player can't accept an already accepted competition`,
    `a player can't decline an already declined competition`,
    `a player can accept and later decline a competition`,
    `a player can decline and later accept a competition`) :: Nil

}