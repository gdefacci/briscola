package com.github.gdefacci.briscola

import com.github.gdefacci.briscola.presentation.BriscolaWebApp
import com.github.gdefacci.briscola.service.AppServices
import com.github.gdefacci.briscola.presentation.AppRoutes
import com.github.gdefacci.briscola.presentation.sitemap.SiteMap
import com.github.gdefacci.briscola.presentation.player.Player
import com.github.gdefacci.free._
import com.github.gdefacci.di.IOC
import scala.util.Try
import org.obl.raz.Authority
import org.obl.raz.Path
import com.github.gdefacci.bdd._
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.presentation.game.GameState
import com.github.gdefacci.briscola.presentation.game.BriscolaEvent
import com.github.gdefacci.briscola.presentation.game.GameStarted
import com.github.gdefacci.briscola.presentation.game.ActiveGameState
import com.github.gdefacci.briscola.presentation.competition.CreatedCompetition
import com.github.gdefacci.briscola.presentation.competition.CompetitionState
import com.github.gdefacci.briscola.presentation.competition.CompetitionAccepted
import com.github.gdefacci.briscola.presentation.competition.CompetitionDeclined

case class CompetitionIntegrationTestState(
  context: IntegrationTestContext,
  siteMap: SiteMap,
  players: Map[String, Player]) extends BaseTestState

trait CompetitionSteps extends BaseSteps[CompetitionIntegrationTestState] {
  import ClientStepFactory._

  lazy val `given an initial application state`: Source = source { () =>
    IOC.get[CompetitionIntegrationTestState](new IntegrationTestModule, IntegratioTestContextModule, Map.empty[String, Player])
  }

  def `given initial players`(players: Seq[String]): Step = step { state =>
    state.http(testClient.createPlayers(state.siteMap, players)).map { pls =>
      state.copy(players = state.players ++ pls.map {
        case (player, resp) => player -> resp
      })
    }
  }

  def `create player`(name: String, password: String): Step = step(state => state.http {
    import TestDecoders.PrivatePlayer._

    for {
      playerResp1 <- post(state.siteMap.players, s"""{ "name":"${name}", "password":"${password}" }""")
      cl <- parse[Player](playerResp1.body)
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

  def `player accept the competition`(name: String): Expectation = expectation(httpExpect { state =>
    for {
      msgs <- webSocket(state.players(name).webSocket)
      comp = msgs.messages.collectFirst(decodePF[EventAndState[CreatedCompetition, CompetitionState]]).get
      resp <- post(comp.state.accept.get)
    } yield if (resp.is2xx) Ok else Fail(s"cant accept competition ${resp.body}")
  })

  def `player decline the competition`(name: String): Expectation = expectation(httpExpect { state =>
    for {
      msgs <- webSocket(state.players(name).webSocket)
      comp = msgs.messages.collectFirst(decodePF[EventAndState[CreatedCompetition, CompetitionState]]).get
      resp <- post(comp.state.decline.get)
    } yield if (resp.is2xx) Ok else Fail(s"cant decline competition ${resp.body}")
  })
  
}