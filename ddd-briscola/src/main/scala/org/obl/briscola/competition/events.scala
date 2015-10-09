package org.obl.briscola
package competition

import player._

sealed trait CompetitionEvent extends org.obl.ddd.Event
sealed trait ClientCompetitionEvent extends CompetitionEvent

case class CreatedCompetition(id:CompetitionId, issuer:Player, competition:Competition) extends ClientCompetitionEvent
case class ConfirmedCompetition(competition:Competition) extends CompetitionEvent 

case class CompetitionAccepted(player:PlayerId) extends ClientCompetitionEvent
case class CompetitionDeclined(player:PlayerId, reason:Option[String]) extends ClientCompetitionEvent

case class CompetitonGameHasBeenSet(gameId:GameId) extends CompetitionEvent