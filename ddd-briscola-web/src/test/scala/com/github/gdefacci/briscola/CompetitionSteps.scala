package com.github.gdefacci.briscola

import scala.util.Try

import com.github.gdefacci.briscola.presentation.BriscolaWebApp
import com.github.gdefacci.briscola.presentation.sitemap.SiteMap
import com.github.gdefacci.briscola.presentation.player.Player
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.presentation.game.GameState
import com.github.gdefacci.briscola.presentation.game.BriscolaEvent
import com.github.gdefacci.briscola.presentation.game.GameStarted
import com.github.gdefacci.briscola.presentation.game.ActiveGameState
import com.github.gdefacci.briscola.presentation.competition.CreatedCompetition
import com.github.gdefacci.briscola.presentation.competition.CompetitionState
import com.github.gdefacci.briscola.presentation.competition.CompetitionAccepted
import com.github.gdefacci.briscola.presentation.competition.CompetitionDeclined

import com.github.gdefacci.free._
import com.github.gdefacci.di.IOC
import com.github.gdefacci.bdd._

case class CompetitionIntegrationTestState(
  context: IntegrationTestContext,
  siteMap: SiteMap,
  players: Map[String, Player]) extends BaseTestState

trait CompetitionSteps extends BaseSteps[CompetitionIntegrationTestState] {
  import ClientStepFactory._

  lazy val `given an initial application state`: Source = source { () =>
    IOC.get[CompetitionIntegrationTestState](new IntegrationTestModule, IntegratioTestContextModule, InitialDataModule)
  }

  def `given initial players`(players: Seq[String]): Step = step { state =>
    state.copy(players = state.players ++ state.http(testClient.createPlayers(state.siteMap, players)))
  }

  def `create player`(name: String, password: String): Step = step(state => state.http {
    for {
      cl <- testClient.createPlayer(state.siteMap, name, password)
    } yield state.copy(players = state.players + (name -> cl))
  })

  import TestDecoders.{ competitionStateDecode, stateAndEventDecoder, decodePF, activeGameStateDecode }
  import TestDecoders.CompetitionEventDecoders._
  import TestDecoders.GameEventDecoders._

  import argonaut.StringWrap._

  def `a valid created competition event`: Predicate[String] = predicate { str =>
    str.decode[EventAndState[CreatedCompetition, CompetitionState]].isRight
  }

  def `an accepted competition event`: Predicate[String] = predicate { str =>
    str.decode[EventAndState[CompetitionAccepted, CompetitionState]].isRight
  }

  def `a declined competition event`: Predicate[String] = predicate { str =>
    str.decode[EventAndState[CompetitionDeclined, CompetitionState]].isRight
  }

  def `a game started event`: Predicate[String] = predicate { str =>
    str.decode[EventAndState[GameStarted, ActiveGameState]].isRight
  }

  def `player accept the competition`(name: String): Expectation = expectation { state =>
    state.http( testClient.playerAcceptTheCompetition(state.players(name).webSocket) )
  }

  def `player decline the competition`(name: String): Expectation = expectation { state =>
    state.http(testClient.playerDeclineTheCompetition(state.players(name).webSocket))
  }

}