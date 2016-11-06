package com.github.gdefacci.briscola

import com.github.gdefacci.briscola.presentation.sitemap.SiteMap
import com.github.gdefacci.free._
import com.github.gdefacci.di.IOC
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.presentation.game.GameStarted
import com.github.gdefacci.briscola.presentation.game.ActiveGameState
import com.github.gdefacci.briscola.presentation.competition.CreatedCompetition
import com.github.gdefacci.briscola.presentation.competition.CompetitionState
import com.github.gdefacci.briscola.presentation.game.GameState
import com.github.gdefacci.briscola.game.Card
import scala.util.Failure
import scala.util.Success
import com.github.gdefacci.briscola.presentation.game.BriscolaEvent
import scala.util.Try
import com.github.gdefacci.bdd.Ok
import com.github.gdefacci.bdd.Fail
import com.github.gdefacci.bdd.SelfDescribeF1
import com.github.gdefacci.bdd.selfDescribe
import com.github.gdefacci.briscola.presentation.game.PlayerState
import org.obl.raz.Path
import com.github.gdefacci.briscola.presentation.player.Player

case class GameIntegrationTestState(
  context: IntegrationTestContext,
  siteMap: SiteMap,
  players: Map[String, Player],
  gameByPlayer: Map[String, GameState]) extends BaseTestState

class GameSteps extends BaseSteps[GameIntegrationTestState] {
  import ClientStepFactory._

  lazy val `given an initial application state`: Source = source { () =>
    IOC.get[GameIntegrationTestState](new IntegrationTestModule, IntegratioTestContextModule, Map.empty[String, Player], Map.empty[String, GameState])
  }

  import TestDecoders.{ competitionStateDecode, stateAndEventDecoder, decodePF, activeGameStateDecode, gameStateDecode, playerStateDecode }
  import TestDecoders.CompetitionEventDecoders._
  import TestDecoders.GameEventDecoders._
  
  def `given initial players`(players: Seq[String]): Step = step { state =>
    state.http(testClient.createPlayers(state.siteMap, players)).map { pls =>
      state.copy(players = state.players ++ pls.map {
        case (player, resp) => player -> resp
      })
    }
  }

  def `players accept the competition starting the game`(players: List[String]): Step = step(st => st.http({
    import scalaz._
    import Scalaz._

    val chs = players.map { player =>
      val pc = st.players(player)
      webSocket(pc.webSocket)
    }
    for {
      compStates <- chs.sequence.map { chs =>
        chs.map(_.messages.collectFirst(decodePF[EventAndState[CreatedCompetition, CompetitionState]]).get.state)
      }
      resps <- compStates.map(comp => post(comp.accept.get)).sequence
      allRespOk = resps.forall(_.is2xx)
      _ <- check(if (allRespOk) Ok else Fail("a player could not accept the competition"))
      res <- updateGameStateMap(st)
    } yield {
      res
    }
  }))

  private def updateGameStateMap(st: GameIntegrationTestState): FreeStep[GameIntegrationTestState] = {
    import scalaz._
    import Scalaz._

    val playersList = st.players.keys.toList
    val chs = playersList.map { player =>
      val pc = st.players(player)
      webSocket(pc.webSocket).map(player -> _)
    }
    for {
      gms <- chs.sequenceU.map { chs =>
        chs.map { 
          case (player, ch) => player -> ch.messages.collect(decodePF[EventAndState[BriscolaEvent, GameState]]).last.state 
        }
      }
    } yield st.copy(gameByPlayer = gms.toMap)
  }
  
  def playerPlaysCard(gm:ActiveGameState, card: PlayerState => Card) = (for {
    psResp <- get(gm.playerState.get)
    ps <- parse[PlayerState](psResp.body)
    c = card(ps)
    resp <- post(gm.playerState.get, s"""{ "number": ${c.number}, "seed":"${c.seed.toString}"  }""")
  } yield resp)

  def `player play`(player: String, card: PlayerState => Card): Step = step { state =>
    state.gameByPlayer(player) match {
      case gm: ActiveGameState =>
        state.http(for {
          resp <- playerPlaysCard(gm, card)
          _ <- check(if (resp.is2xx) Ok else Fail(s"error playing card\n${resp.body}"))
          res <- updateGameStateMap(state)
        } yield res)

      case _ => Failure(new RuntimeException("game is not active"))
    }
  }
  
  lazy val `a random card` = selfDescribe[PlayerState, Card] { gm =>
    val pcd = gm.cards.head
    Card(pcd.number, pcd.seed)
  }

  private def getPlayerNameByPlayerSelf(state:GameIntegrationTestState, slf:Path)= state.players.collectFirst { 
    case (nm, pl) if pl.self.toString == slf.toString => nm
  }
  
  def `current player play`(card: PlayerState => Card): Step = step { state =>
    val curr = state.gameByPlayer.map {
      case (_,gm:ActiveGameState) => gm.currentPlayer
      case _ => throw new RuntimeException("game is not active")
    }.toSet
    assert(curr.size==1)
    val player = getPlayerNameByPlayerSelf(state, curr.head).get
    state.gameByPlayer(player) match {
      case gm: ActiveGameState =>
        state.http {
          for {
            resp <- playerPlaysCard(gm, card)
            _ <- check(if (resp.is2xx) Ok else Fail(s"error playing card\n${resp.body}"))
            res <- updateGameStateMap(state)
          } yield res
        }

      case _ => Failure(new RuntimeException("game is not active"))
    }
  }  
}