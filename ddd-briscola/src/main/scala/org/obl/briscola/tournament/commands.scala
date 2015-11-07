package org.obl.briscola.tournament 

import org.obl.briscola.player.PlayerId
import org.obl.briscola.competition.MatchKind
import org.obl.briscola.FinalGameState
import org.obl.briscola.DroppedGameState
import org.obl.briscola.ActiveGameState
import org.obl.ddd.Command
import org.obl.briscola.player.GamePlayers

sealed trait TournamentCommand extends Command

final case class StartTournament(players:GamePlayers, kind:MatchKind) extends TournamentCommand 
final case class SetTournamentGame(game:ActiveGameState) extends TournamentCommand 
final case class SetGameOutcome(game:FinalGameState) extends TournamentCommand 
final case class DropTournamentGame(game:DroppedGameState) extends TournamentCommand
