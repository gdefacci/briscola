package com.github.gdefacci.briscola
package competition

import player._

sealed trait CompetitionCommand

final case class CreateCompetition(issuer:PlayerId, 
    players:GamePlayers, 
    kind:MatchKind, 
    deadLine:CompetitionStartDeadline/*, FIXME"this functionality needs a timer" timeout:Int */) extends CompetitionCommand
    
final case class AcceptCompetition(player:PlayerId) extends CompetitionCommand 
final case class DeclineCompetition(player:PlayerId, reason:Option[String]) extends CompetitionCommand 