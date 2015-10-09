package org.obl.briscola
package competition

import player._

case class CompetitionId(id:Long)

case class Competition(players:Set[Player], kind:MatchKind, deadline:CompetitionStartDeadline/*, FIXME"this functionality needs a timer" endTime:LocalDateTime */)

sealed trait MatchKind
case object SingleMatch extends MatchKind
case class NumberOfGamesMatchKind(numberOfMatches:Int) extends MatchKind
case class TargetPointsMatchKind(winnerPoints:Int) extends MatchKind

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
sealed trait ClientCompetitionState extends CompetitionState {
  def id:CompetitionId
  def competition:Competition
}

case object EmptyCompetition extends CompetitionState  
case class OpenCompetition(id:CompetitionId, competition:Competition, acceptingPlayers:Set[PlayerId], decliningPlayers:Set[PlayerId]) extends ClientCompetitionState  
case class DroppedCompetition(id:CompetitionId, competition:Competition, acceptingPlayers:Set[PlayerId], decliningPlayers:Set[PlayerId]) extends ClientCompetitionState  
case class FullfilledCompetition(id:CompetitionId, competition:Competition, acceptingPlayers:Set[PlayerId], decliningPlayers:Set[PlayerId], game:Option[GameId]) extends CompetitionState  