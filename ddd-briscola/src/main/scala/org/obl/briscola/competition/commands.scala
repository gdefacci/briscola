package org.obl.briscola
package competition

import player._

sealed trait CompetitionCommand extends org.obl.ddd.Command

case class CreateCompetition(issuer:PlayerId, players:Set[PlayerId], kind:MatchKind, deadLine:CompetitionStartDeadline/*, FIXME"this functionality needs a timer" timeout:Int */) extends CompetitionCommand
case class AcceptCompetition(player:PlayerId) extends CompetitionCommand 
case class DeclineCompetition(player:PlayerId, reason:Option[String]) extends CompetitionCommand 
case class SetCompetitonGame(gameId:GameId) extends CompetitionCommand 