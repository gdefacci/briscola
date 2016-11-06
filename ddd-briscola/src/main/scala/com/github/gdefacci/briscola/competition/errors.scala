package com.github.gdefacci.briscola.competition

import com.github.gdefacci.briscola.game.BriscolaError
import com.github.gdefacci.briscola.player.PlayerId

sealed trait CompetitionError 

case object CompetitionNotStarted extends CompetitionError 
case object CompetitionAlreadyStarted extends CompetitionError 
case object CompetitionAlreadyFinished extends CompetitionError
case object CompetitionDropped extends CompetitionError
case object CompetitionIsNotFullfilled extends CompetitionError
case object CompetitionAlreadyAccepted extends CompetitionError
case object CompetitionAlreadyDeclined extends CompetitionError

final case class CompetioBriscolaError(err:BriscolaError) extends CompetitionError
