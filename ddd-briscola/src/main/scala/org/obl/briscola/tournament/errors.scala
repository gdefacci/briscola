package org.obl.briscola.tournament

import org.obl.ddd.DomainError
import org.obl.briscola.GameState
import org.obl.briscola.BriscolaError

trait TournamentError extends DomainError

case object TournamentNotStarted extends TournamentError 
case object TournamentAlreadyStarted extends TournamentError 
case object TournamentAlreadyCompleted extends TournamentError 
case object TournamentAlreadyDropped extends TournamentError

final case class GameDoesNotBelongTournament(gameState:GameState) extends TournamentError 
final case class TournamentGameError(gameError:BriscolaError) extends TournamentError