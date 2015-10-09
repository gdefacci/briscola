package org.obl.briscola.competition

import org.obl.ddd.DomainError
import org.obl.briscola.BriscolaError

sealed trait CompetitionError extends DomainError

case object CompetitionNotStarted extends CompetitionError 
case object CompetitionAlreadyStarted extends CompetitionError 
case object CompetitionAlreadyFinished extends CompetitionError
case object CompetitionDropped extends CompetitionError
case object CompetitionIsNotFullfilled extends CompetitionError

case class CompetioBriscolaError(err:BriscolaError) extends CompetitionError
