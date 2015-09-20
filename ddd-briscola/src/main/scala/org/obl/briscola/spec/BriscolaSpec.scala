package org.obl.briscola
package spec

import org.obl.ddd._
import org.obl.ddd.spec._
import org.obl.briscola.player._

trait BriscolaSpec extends Spec[GameState, BriscolaCommand, BriscolaEvent, BriscolaError] {

  def initialState = EmptyGameState

  val decider = new GameDecider {

    def nextId: GameId = GameId(1)
    def playerById(playerId: PlayerId) = Some(Player(playerId, playerId.id.toString))

  }

  val evolver = new GameEvolver {}

}