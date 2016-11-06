package com.github.gdefacci.briscola
package tournament 

import com.github.gdefacci.briscola.player.Player
import com.github.gdefacci.briscola.competition.MatchKind
import com.github.gdefacci.briscola.game.{ActiveGameState, FinalGameState, DropReason}
import com.github.gdefacci.briscola.player.GamePlayers

sealed trait TournamentEvent extends Event

final case class TournamentStarted(player:GamePlayers, kind:MatchKind) extends TournamentEvent
final case class TournamentGameHasStarted(game:ActiveGameState) extends TournamentEvent
final case class TournamentGameHasFinished(game:FinalGameState) extends TournamentEvent
final case class TournamentHasBeenDropped(dropReason:DropReason) extends TournamentEvent