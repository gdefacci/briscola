package com.github.gdefacci.briscola.presentation

import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.competition._
import com.github.gdefacci.briscola.game._

final case class PlayerGameEvent(playerId:PlayerId, gameId:GameId, event: BriscolaEvent)
final case class PlayerActiveGameState(playerId:PlayerId, game:ActiveGameState)

final case class PlayerCompetitionEvent(playerId:PlayerId, competitionId:CompetitionId, event: ClientCompetitionEvent)
final case class PlayerCompetitionState(playerId:PlayerId, competitionState:ClientCompetitionState)