package org.obl.briscola
package service
package player

import org.obl.briscola.player.PlayerId
import org.obl.briscola.competition.CompetitionId
import org.obl.briscola.competition.ClientCompetitionState
import org.obl.briscola.competition.ClientCompetitionEvent

sealed trait PlayerEvent extends org.obl.ddd.Event

final case class PlayerLogOn(playerId:PlayerId) extends PlayerEvent
final case class PlayerLogOff(playerId:PlayerId) extends PlayerEvent


final case class PlayerGameEvent(playerId:PlayerId, gameId:GameId, event: BriscolaEvent)
final case class PlayerActiveGameState(playerId:PlayerId, game:ActiveGameState)

final case class PlayerCompetitionEvent(playerId:PlayerId, competitionId:CompetitionId, event: ClientCompetitionEvent)
final case class PlayerCompetitionState(playerId:PlayerId, competitionState:ClientCompetitionState)