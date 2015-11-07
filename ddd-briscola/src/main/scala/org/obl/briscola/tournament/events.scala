package org.obl.briscola.tournament 

import org.obl.briscola.player.Player
import org.obl.briscola.competition.MatchKind
import org.obl.briscola.ActiveGameState
import org.obl.ddd.Event
import org.obl.briscola.FinalGameState
import org.obl.briscola.DropReason
import org.obl.briscola.player.GamePlayers

sealed trait TournamentEvent extends Event

final case class TournamentStarted(player:GamePlayers, kind:MatchKind) extends TournamentEvent
final case class TournamentGameHasStarted(game:ActiveGameState) extends TournamentEvent
final case class TournamentGameHasFinished(game:FinalGameState) extends TournamentEvent
final case class TournamentHasBeenDropped(dropReason:DropReason) extends TournamentEvent