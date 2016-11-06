package com.github.gdefacci.briscola
package spec

import scalaz.{ -\/, \/, \/- }

import com.github.gdefacci.ddd._
import com.github.gdefacci.bdd._
import com.github.gdefacci.briscola.player.{ PlayerId, Player }
import com.github.gdefacci.briscola.player.Team
import com.github.gdefacci.briscola.game._

object GameCommandRunner {

  def withPlayers(players: Seq[Player]): CommandRunner[GameState, BriscolaCommand, BriscolaEvent, BriscolaError] = {

    val decider = new GameDecider {
      def nextId: GameId = GameId(1)
      def playerById(playerId: PlayerId) = players.find(_.id == playerId)
    }

    val evolver = new GameEvolver {}

    com.github.gdefacci.ddd.Runner.apply(decider, evolver)
  }

}

case class GameTestState(
  runner: CommandRunner[GameState, BriscolaCommand, BriscolaEvent, BriscolaError],
  result: BriscolaError \/ (Seq[BriscolaEvent], GameState)) extends EventsTestState[GameState, BriscolaEvent, BriscolaError]
  
class GameSteps extends EventsSteps[GameTestState, GameState, BriscolaEvent, BriscolaError] {

  def `the initial game state is`(gameState:GameState):Step = step { state =>
    state.result match {
      case err @ -\/(_) => state
      case \/-((initialEvents, _)) =>
        state.copy(result = \/-(initialEvents -> gameState))
    }
  } 
  
  def `Given an empty game with logged players`(players:Player*): Source = source { () =>
    GameTestState(GameCommandRunner.withPlayers(players), \/-(Nil, EmptyGameState))
  }

  def `issue the command`(command: BriscolaCommand): Step = step { state =>
    val result = state.result match {
      case err @ -\/(_) => err
      case \/-((initialEvents, initialState)) =>
        state.runner(initialState, command) match {
          case err @ -\/(_) => err
          case \/-((newEvents, newState)) => \/-((initialEvents ++ newEvents) -> newState)
        }
    }
    state.copy(result = result)
  }

  def `has a deck with a total of cards`(count:Int):Predicate[GameState] = predicate { 
    case ActiveGameState(_,_,deck,_,_,_) => deck.cards.length == count
    case _  => false
  }
  
  def `include player`(player:Player):Predicate[GameState] = predicate { 
    case gm:ActiveGameState => gm.players.map(_.id).contains(player.id)
    case gm:FinalGameState => gm.players.map(_.id).contains(player.id)
    case _  => false
  }
  
  lazy val `is an ActiveGameState`:Predicate[GameState] = predicate { 
    case _:ActiveGameState  => true
    case _  => false
  }
  
  lazy val `is a FinalGameState`:Predicate[GameState] = predicate { 
     case _:FinalGameState => true
    case _  => false
  }
  
  def `the sum off all players points is`(total:Int):Expectation = expectation { state =>
    state.result.map {
      case (_, result) =>
        result match {
          case gm:FinalGameState if gm.players.map(_.points).sum == total => 
            Ok
          case gm:ActiveGameState if gm.players.map(_.points).sum == total => 
            Ok
          case gm:FinalGameState => 
            Fail(s"expecting $total points got ${gm.players.map(_.points).sum}")
          case gm:ActiveGameState => 
            Fail(s"expecting $total points got ${gm.players.map(_.points).sum}")
          case gm => 
            Fail(s"expecting $total points got a game with no points ${gm}")
        }
    }.toOption getOrElse Fail("Failed result")
  }
  
  lazy val `the current player play a random card`:Step = step { state =>
    state.result match {
      case \/-((events, st @ ActiveGameState(_,_,_,_,_,_))) => 
        val id = st.id
        val pid = st.currentPlayer.id
        val aCard = st.currentPlayer.cards.toSeq(0)
        `issue the command`(PlayCard(pid, aCard)).run(state)
      case _ => state
    }
  }
  
  def `repeat step until state`(stp:Step, predicate:GameState => Boolean):Step = step { state =>
    state.result match {
      case -\/(_) => state
      case \/-((events, gameState)) =>
        if (predicate(gameState)) state
        else {
          val ns = stp.run(state)
          `repeat step until state`(stp, predicate).run(ns)
        }
    }
  }
  
  def `the winner team is`(team:Team):Expectation = expectation { state =>
    state.result.map { 
      case (_, gm:FinalGameState) if gm.teamScoresOrderByPoints.get.head.team == team => Ok
      case (_, gm:FinalGameState) => Fail(s"Expecting winner $team but was ${gm.teamScoresOrderByPoints.head}")
      case (_, gm) => Fail(s"Expecting a final game got $gm")
    }.toOption getOrElse Fail("Failed result")
  }
}