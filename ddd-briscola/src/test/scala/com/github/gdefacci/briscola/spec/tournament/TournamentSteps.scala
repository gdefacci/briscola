package com.github.gdefacci.briscola.spec
package tournament

import com.github.gdefacci.ddd.CommandRunner
import com.github.gdefacci.briscola.tournament._
import com.github.gdefacci.briscola.player._
import scalaz.{ -\/, \/, \/- }

import com.github.gdefacci.bdd._

object TournamentCommandRunner {

  def withPlayers(players: Seq[Player]): CommandRunner[TournamentState, TournamentCommand, TournamentEvent, TournamentError] = {

    val decider = new TournamentDecider {
      def playerById(playerId: PlayerId) = players.find(_.id == playerId)
    }

    val evolver = new TournamentEvolver {
      def nextId: TournamentId = TournamentId(1)
    }

    com.github.gdefacci.ddd.Runner.apply(decider, evolver)
  }

}

case class TournamentTestState(
  runner: CommandRunner[TournamentState, TournamentCommand, TournamentEvent, TournamentError],
  result: TournamentError \/ (Seq[TournamentEvent], TournamentState)) extends EventsTestState[TournamentState, TournamentEvent, TournamentError]


trait TournamentSteps extends EventsSteps[TournamentTestState, TournamentState, TournamentEvent, TournamentError] {
  
  def `Given that logged players are`(players:Player*): Source = source { () =>
    TournamentTestState(TournamentCommandRunner.withPlayers(players), \/-(Nil, EmptyTournamentState))
  }
  
  def `issue the command`(command: TournamentCommand): Step = step { state =>
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
  
  lazy val `is a completed tournament`:Predicate[TournamentState] = predicate { 
    case _:CompletedTournamentState  => true
    case _  => false
  }
  
  lazy val `is an active tournament`:Predicate[TournamentState] = predicate { 
    case _:ActiveTournamentState  => true
    case _  => false
  }
  
}