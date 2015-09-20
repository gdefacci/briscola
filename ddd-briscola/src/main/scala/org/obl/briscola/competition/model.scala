package org.obl.briscola
package competition

import player._

case class CompetitionId(id:Long)

case class Competition(id:CompetitionId, players:Set[Player], kind:MatchKind, deadlineKind:CompetitionStartDeadline/*, FIXME"this functionality needs a timer" endTime:LocalDateTime */)

sealed trait MatchKind
case object SingleMatch extends MatchKind
case class Tournament(numberOfMatches:Int) extends MatchKind
case class TargetTournament(winnerPoints:Int) extends MatchKind

sealed trait CompetitionStartDeadline

object CompetitionStartDeadline {

  /**
   * The match is created if all players join the match. The match can be created before the timeout expires.
   * The game will have players.length players
   */
  case object AllPlayers extends CompetitionStartDeadline
  
  /**
   * When count players join the match, the match is created. The match can be created before the timeout expires.
   * The game will have count players
   */
  case class OnPlayerCount(count:Int) extends CompetitionStartDeadline
  
  /**
   * FIXME This functionality needs a timer
   * 
   * The match is created if at least count players join the match. The match will be created after the timeout expires.
   * The game will have n players, where :
   * 
   * playes.length >= n >= count
   */
//  case class AtLeastPlayerCount(count:Int) extends CompetitionStartDeadline

}

sealed trait CompetitionState extends org.obl.ddd.State

case object EmptyCompetition extends CompetitionState  
case class OpenCompetition(competition:Competition, acceptingPlayers:Set[PlayerId], decliningPlayers:Set[PlayerId]) extends CompetitionState  
case class DroppedCompetition(competition:Competition, acceptingPlayers:Set[PlayerId], decliningPlayers:Set[PlayerId]) extends CompetitionState  
case class FullfilledCompetition(competition:Competition, acceptingPlayers:Set[PlayerId], decliningPlayers:Set[PlayerId]) extends CompetitionState  