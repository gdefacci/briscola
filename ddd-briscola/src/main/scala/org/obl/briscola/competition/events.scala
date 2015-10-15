package org.obl.briscola
package competition

import player._

sealed trait CompetitionEvent extends org.obl.ddd.Event
sealed trait ClientCompetitionEvent extends CompetitionEvent

final case class CreatedCompetition(id:CompetitionId, issuer:Player, competition:Competition) extends ClientCompetitionEvent
final case class ConfirmedCompetition(competition:Competition) extends CompetitionEvent 

final case class CompetitionAccepted(player:PlayerId) extends ClientCompetitionEvent
final case class CompetitionDeclined(player:PlayerId, reason:Option[String]) extends ClientCompetitionEvent