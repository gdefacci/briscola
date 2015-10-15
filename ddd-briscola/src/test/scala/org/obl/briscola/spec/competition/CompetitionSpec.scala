package org.obl.briscola.spec.competition

import org.obl.ddd._
import org.obl.ddd.spec._
import org.obl.briscola.player._
import org.obl.briscola.competition._

trait CompetitionSpec extends Spec[CompetitionState, CompetitionCommand, CompetitionEvent, CompetitionError] {
  
  def initialState = EmptyCompetition

  val decider = new CompetitionDecider {

    def nextId: CompetitionId = CompetitionId(1)
    def playerById(playerId: PlayerId) = Some(Player(playerId, playerId.id.toString, ""))

  }

  val evolver = new CompetitionEvolver {}

}