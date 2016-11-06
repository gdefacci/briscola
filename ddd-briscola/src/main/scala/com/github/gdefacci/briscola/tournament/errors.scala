package com.github.gdefacci.briscola.tournament

import com.github.gdefacci.briscola.game.{GameState, BriscolaError}

trait TournamentError 

case object TournamentNotStarted extends TournamentError 
case object TournamentAlreadyStarted extends TournamentError 
case object TournamentAlreadyCompleted extends TournamentError 
case object TournamentAlreadyDropped extends TournamentError

final case class GameDoesNotBelongTournament(gameState:GameState) extends TournamentError 
final case class TournamentGameError(gameError:BriscolaError) extends TournamentError