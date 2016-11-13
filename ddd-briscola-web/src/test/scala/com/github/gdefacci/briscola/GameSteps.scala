package com.github.gdefacci.briscola

import org.obl.raz.Path

import com.github.gdefacci.bdd._
import com.github.gdefacci.briscola.game.Card
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.presentation.game.ActiveGameState
import com.github.gdefacci.briscola.presentation.game.BriscolaEvent
import com.github.gdefacci.briscola.presentation.game.CardPlayed
import com.github.gdefacci.briscola.presentation.game.FinalGameState
import com.github.gdefacci.briscola.presentation.game.GameState
import com.github.gdefacci.briscola.presentation.game.PlayerState
import com.github.gdefacci.briscola.presentation.player.Player
import com.github.gdefacci.briscola.presentation.sitemap.SiteMap
import com.github.gdefacci.di.IOC
import com.github.gdefacci.free._
import scala.util.Try

case class GameIntegrationTestState(
  context: IntegrationTestContext,
  siteMap: SiteMap,
  players: Map[String, Player],
  playerStateMap: Map[String, (Path, PlayerState)],
  gameState: Option[GameState]) extends BaseTestState

class GameSteps extends BaseSteps[GameIntegrationTestState] {
  import ClientStepFactory._

  lazy val `given an initial application state`: Source = source { () =>
    IOC.get[GameIntegrationTestState](new IntegrationTestModule, IntegratioTestContextModule, InitialDataModule)
  }

  import TestDecoders.{ competitionStateDecode, stateAndEventDecoder, activeGameStateDecode, gameStateDecode, playerStateDecode}
  import TestDecoders.CompetitionEventDecoders._
  import TestDecoders.GameEventDecoders._

  def `given initial players`(players: Seq[String]): Step = step { state =>
    state.copy(players = state.players ++ state.http(testClient.createPlayers(state.siteMap, players)))
  }

  def `players accept the competition starting the game`(players: List[String]): Step = step(st => st.http {
    import scalaz._
    import Scalaz._

    for {
      _ <- players.map { player => testClient.playerAcceptTheCompetition(st.players(player).webSocket) }.sequenceU
      res <- updatedGameStateMap(st)
    } yield {
      res
    }
  })

  private def updatedGameStateMap(st: GameIntegrationTestState): FreeStep[GameIntegrationTestState] = {
    import scalaz._
    import Scalaz._

    val gmStates = for {
      pl <- st.players.keys.toList
    } yield testClient.messagesOf[EventAndState[BriscolaEvent, GameState]](st.players(pl).webSocket).map(pl -> _.last.state)
    
    for {
      gms <- gmStates.sequenceU
      gameState = gms.map(_._2).map {
        case gm: ActiveGameState => gm.copy(playerState = None)
        case gm => gm
      }.toSet
      _ <- check {
        if (gameState.size != 1) Fail("players are in different game state")
        else Ok
      }
      playerStatePathMap = gms.flatMap {
        case (player, ActiveGameState(_, _, _, _, _, _, _, _, _, Some(playerState), _)) => Map(player -> playerState)
        case _ => Map.empty[String, Path]
      }
      playerStateMap <- playerStatePathMap.map {
        case (player, pth) => for {
          plSt <- testClient.getAndParse[PlayerState](pth, "player state")
        } yield player -> (pth -> plSt)
      }.sequenceU
    } yield st.copy(gameState = Some(gameState.head), playerStateMap = playerStateMap.toMap)
  }

  lazy val `a random card` = selfDescribe[PlayerState, Card] { gm =>
    val pcd = gm.cards.head
    Card(pcd.number, pcd.seed)
  }

  private def getPlayerNameByPlayerSelf(state: GameIntegrationTestState, slf: Path) = state.players.collectFirst {
    case (nm, pl) if pl.self.toString == slf.toString => nm
  }

  def `current player play`(card: PlayerState => Card): Step = step { state =>
    val curr = state.gameState.map {
      case (gm: ActiveGameState) => gm.currentPlayer
      case _ => throw new RuntimeException("game is not active")
    } getOrElse { throw new RuntimeException("game is not active") }
    
    val player = getPlayerNameByPlayerSelf(state, curr).get
    val playerState = state.playerStateMap(player)
    state.http {
      for {
        resp <- testClient.playerPlaysCard(playerState._1, card)
        _ <- check(if (resp.is2xx) Ok else Fail(s"error playing card\n${resp.body}"))
        res <- updatedGameStateMap(state)
      } yield res
    }
  }

  import argonaut.StringWrap._

  def `a card played event`: Predicate[String] = predicate { str =>
    str.decode[EventAndState[CardPlayed, ActiveGameState]].isRight
  }
  
  def `game is finished`:Predicate[GameIntegrationTestState] = predicate { st =>
    st.gameState.map(_ match {
      case gm:FinalGameState => true
      case _ => false
    }).getOrElse(false)
  }
}