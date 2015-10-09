package org.obl.briscola
package competition

import player._
import org.obl.ddd._
import scalaz.{-\/, \/, \/-}
import java.time.LocalDateTime

trait CompetitionDecider extends Decider[CompetitionState, CompetitionCommand, CompetitionEvent, CompetitionError] {
  
  def nextId:CompetitionId
  def playerById(id:PlayerId):Option[Player]
  
  def apply(s:CompetitionState, cmd:CompetitionCommand):CompetitionError \/ Seq[CompetitionEvent] = {
    (s -> cmd) match {
      case (EmptyCompetition, CreateCompetition(issuer, plyrs, kind, deadLine)) =>
        val players = plyrs + issuer
        GameValidator.checkPlayersNumber(players) match {
          case Some(err) => -\/(CompetioBriscolaError(err))
          case None => GameValidator.checkAllPlayersExists(playerById, players).map { players =>
              val issr = players.find(_.id == issuer).get
              Seq(CreatedCompetition(nextId, issr, Competition(players, kind, deadLine))) 
            }.leftMap(CompetioBriscolaError(_))
        }
      case (comp:OpenCompetition, AcceptCompetition(playerId)) =>
        if (!comp.competition.players.exists(_.id == playerId)) {
          -\/(CompetioBriscolaError(InvalidPlayer(playerId)))
        } else {
          \/-(Seq(CompetitionAccepted(playerId)))
        }
      case (comp:OpenCompetition, DeclineCompetition(playerId, reason)) =>
        if (!comp.competition.players.exists(_.id == playerId)) {
          -\/(CompetioBriscolaError(InvalidPlayer(playerId)))
        } else {
          \/-(Seq(CompetitionDeclined(playerId, reason)))
        }
        // CompetitionIsNotFullfilled
      case (EmptyCompetition, _) => -\/(CompetitionNotStarted)
      case (comp:OpenCompetition, cmd:CreateCompetition) => -\/(CompetitionAlreadyStarted)
      case (c:DroppedCompetition, _) => -\/(CompetitionDropped)  
      case (FullfilledCompetition(id,_,_,_,None), SetCompetitonGame(gid)) => \/-(Seq(CompetitonGameHasBeenSet(gid)))  
      case (c:FullfilledCompetition, _) => -\/(CompetitionAlreadyFinished)
      case (_, SetCompetitonGame(_)) => -\/(CompetitionIsNotFullfilled)
    }
  }
  
} 

trait CompetitionEvolver extends Evolver[CompetitionState, CompetitionEvent] {
 
  def isFullfilled(comp:Competition, players:Set[PlayerId]) = 
    comp.deadline match {
      case CompetitionStartDeadline.AllPlayers => comp.players.map(_.id) == players
      case CompetitionStartDeadline.OnPlayerCount(n) => players.size == n
    }

  def isDropped(comp:Competition, decliningPlayers:Set[PlayerId]) = {
    comp.deadline match {
      case CompetitionStartDeadline.AllPlayers => decliningPlayers.nonEmpty
      case CompetitionStartDeadline.OnPlayerCount(n) => (comp.players.size - decliningPlayers.size) < n
    }
  }
  
  def apply(s:CompetitionState, event:CompetitionEvent):CompetitionState = {
    (s -> event) match {
      case (EmptyCompetition, CreatedCompetition(id, issuer, comp)) => 
        OpenCompetition(id, comp,Set(issuer.id), Set.empty)
        
      case (OpenCompetition(id,competition,acceptingPlayers,decliningPlayers), CompetitionAccepted(pid)) =>
        if (isFullfilled(competition, acceptingPlayers + pid)) 
        	FullfilledCompetition(id, competition,acceptingPlayers + pid, decliningPlayers - pid, None)
        else
          OpenCompetition(id,competition,acceptingPlayers + pid, decliningPlayers - pid)
          
      case (OpenCompetition(id,competition,acceptingPlayers,decliningPlayers), CompetitionDeclined(pid, rsn)) => 
        if (isDropped(competition, decliningPlayers + pid)) 
        	DroppedCompetition(id, competition,acceptingPlayers - pid, decliningPlayers + pid)
        else
          OpenCompetition(id, competition, acceptingPlayers - pid,  decliningPlayers + pid)
      
      case (FullfilledCompetition(id, comp,accpt,decl, None), CompetitonGameHasBeenSet(gid))  => 
          FullfilledCompetition(id, comp,accpt,decl, Some(gid))
          
       case _ => {
        throw new RuntimeException(s"forbidden condition state:${s} event:${event}")
        
      }
    }
  }  
}