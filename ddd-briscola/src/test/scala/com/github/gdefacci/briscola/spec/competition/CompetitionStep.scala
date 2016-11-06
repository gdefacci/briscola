package com.github.gdefacci.briscola.spec
package competition

import scalaz.{ -\/, \/, \/- }
import com.github.gdefacci.briscola.competition._
import com.github.gdefacci.bdd._

import com.github.gdefacci.ddd.CommandRunner
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.player.Player
import com.github.gdefacci.ddd._

object CompetitionCommandRunner {

  def withPlayers(players: Seq[Player]):CommandRunner[CompetitionState, CompetitionCommand, CompetitionEvent, CompetitionError] = {

    val decider = new CompetitionDecider {
      def nextId: CompetitionId = CompetitionId(1)
      def playerById(playerId: PlayerId) = players.find(_.id == playerId)
    }

    val evolver = new CompetitionEvolver {}

    com.github.gdefacci.ddd.Runner.apply(decider, evolver)
  }

}

case class CompetitionTestState(
  runner: CommandRunner[CompetitionState, CompetitionCommand, CompetitionEvent, CompetitionError],
  result: CompetitionError \/ (Seq[CompetitionEvent], CompetitionState)) extends EventsTestState[CompetitionState, CompetitionEvent, CompetitionError]

  
object CompetitionSteps extends EventsSteps[CompetitionTestState, CompetitionState, CompetitionEvent, CompetitionError] {

  def `Given an empty competition with players`(players:Player*): Source = source { () =>
    CompetitionTestState(CompetitionCommandRunner.withPlayers(players), \/-(Nil, EmptyCompetition))
  }

  def `issue the command`(command: CompetitionCommand): Step = step { state =>
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

}