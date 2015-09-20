package org.obl.briscola
package competition

import player._

sealed trait CompetitionEvent extends org.obl.ddd.Event

case class CreatedCompetition(issuer:Player, competition:Competition) extends CompetitionEvent 
case class ConfirmedCompetition(competition:Competition) extends CompetitionEvent 

case class CompetitionAccepted(player:PlayerId, competition:CompetitionId) extends CompetitionEvent 
case class CompetitionDeclined(player:PlayerId, competition:CompetitionId, reason:Option[String]) extends CompetitionEvent 
