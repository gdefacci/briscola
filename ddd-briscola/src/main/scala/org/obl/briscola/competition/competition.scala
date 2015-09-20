
package org.obl.briscola
package competition

import player._
import org.obl.ddd._
import scalaz.{-\/, \/, \/-}
import java.time.LocalDateTime

trait CompetitionDecider extends Decider[CompetitionState, CompetitionCommand, CompetitionEvent, BriscolaError] {
  
  def nextId:CompetitionId
  def playerById(id:PlayerId):Option[Player]
  
  def apply(s:CompetitionState, cmd:CompetitionCommand):BriscolaError \/ Seq[CompetitionEvent] = {
    (s -> cmd) match {
      case (EmptyCompetition, CreateCompetition(issuer, plyrs, kind, deadLine)) =>
        val players = plyrs + issuer
        GameValidator.checkPlayersNumber(players) match {
          case Some(err) => -\/(err)
          case None => GameValidator.checkAllPlayersExists(playerById, players).map { players =>
              val issr = players.find(_.id == issuer).get
              Seq(CreatedCompetition(issr, Competition(nextId, players, kind, deadLine))) 
            }
        }
      case (comp:OpenCompetition, AcceptCompetition(playerId, competitionId)) =>
        if (!comp.competition.players.exists(_.id == playerId)) {
          -\/(InvalidPlayer(playerId))
        } else {
          \/-(Seq(CompetitionAccepted(playerId, competitionId)))
        }
      case (comp:OpenCompetition, DeclineCompetition(playerId, competitionId, reason)) =>
        if (!comp.competition.players.exists(_.id == playerId)) {
          -\/(InvalidPlayer(playerId))
        } else {
          \/-(Seq(CompetitionDeclined(playerId, competitionId, reason)))
        }
      case (EmptyCompetition, _) => -\/(GameNotStarted)
      case (comp:OpenCompetition, cmd:CreateCompetition) => -\/(GameAlreadyStarted)
      case (c:DroppedCompetition, _) => -\/(GameDropped)  
      case (c:FullfilledCompetition, _) => -\/(GameAlreadyFinished)  
    }
  }
  
} 

trait CompetitionEvolver extends Evolver[CompetitionState, CompetitionEvent] {
 
  def isFullfilled(comp:Competition, players:Set[PlayerId]) = 
    comp.deadlineKind match {
      case CompetitionStartDeadline.AllPlayers => comp.players.map(_.id) == players
      case CompetitionStartDeadline.OnPlayerCount(n) => players.size == n
    }

  def isDropped(comp:Competition, decliningPlayers:Set[PlayerId]) = {
    comp.deadlineKind match {
      case CompetitionStartDeadline.AllPlayers => decliningPlayers.nonEmpty
      case CompetitionStartDeadline.OnPlayerCount(n) => (comp.players.size - decliningPlayers.size) < n
    }
  }
  
  def apply(s:CompetitionState, event:CompetitionEvent):CompetitionState = {
    (s -> event) match {
      case (EmptyCompetition, CreatedCompetition(issuer, comp)) => 
        OpenCompetition(comp,Set(issuer.id), Set.empty)
        
      case (comp:OpenCompetition, CompetitionAccepted(pid, cid)) =>
        if (isFullfilled(comp.competition, comp.acceptingPlayers + pid)) 
        	FullfilledCompetition(comp.competition,comp.acceptingPlayers + pid, comp.decliningPlayers - pid)
        else
          OpenCompetition(comp.competition,comp.acceptingPlayers + pid, comp.decliningPlayers - pid)
          
      case (comp:OpenCompetition, CompetitionDeclined(pid, cid, rsn)) => 
        if (isDropped(comp.competition, comp.decliningPlayers + pid)) 
        	DroppedCompetition(comp.competition,comp.acceptingPlayers - pid, comp.decliningPlayers + pid)
        else
          OpenCompetition(comp.competition,comp.acceptingPlayers - pid, comp.decliningPlayers + pid)
          
       case _ => {
        throw new RuntimeException(s"forbidden condition state:${s} event:${event}")
        
      }
    }
  }  
}