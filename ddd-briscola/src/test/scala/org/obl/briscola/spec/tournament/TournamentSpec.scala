package org.obl.briscola.spec.tournament

import org.obl.ddd._
import org.obl.ddd.spec._
import org.obl.briscola.player._
import org.obl.briscola.tournament._

trait TournamentSpec extends Spec[TournamentState, TournamentCommand, TournamentEvent, TournamentError] {
  
  def initialState = EmptyTournamentState

  val decider = new TournamentDecider {
    def playerById(playerId: PlayerId):Option[Player] = Some(Player(playerId, playerId.id.toString, ""))
  }

  val evolver = new TournamentEvolver {
    def nextId: TournamentId = TournamentId(1)
  }

  
}