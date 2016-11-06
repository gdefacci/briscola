package com.github.gdefacci.briscola.tournament 

import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.competition.MatchKind
import com.github.gdefacci.briscola.game.{FinalGameState, DroppedGameState, ActiveGameState}
import com.github.gdefacci.briscola.player.GamePlayers

sealed trait TournamentCommand 

final case class StartTournament(players:GamePlayers, kind:MatchKind) extends TournamentCommand 
final case class SetTournamentGame(game:ActiveGameState) extends TournamentCommand 
final case class SetGameOutcome(game:FinalGameState) extends TournamentCommand 
final case class DropTournamentGame(game:DroppedGameState) extends TournamentCommand
