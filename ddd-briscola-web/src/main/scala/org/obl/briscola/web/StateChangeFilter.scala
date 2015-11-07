package org.obl.briscola
package web

import org.obl.ddd.StateChange
import argonaut.EncodeJson
import org.obl.briscola.player.PlayerId
import presentation.EventAndState
import org.obl.briscola.service._
import org.obl.briscola.service.player.PlayerEvent
import org.obl.briscola.player.Player

import StateChangeFilter._
import player._
import competition._
import org.obl.briscola.player.GamePlayers

object StateChangeFilter {
  
  type StateChangeFilter[S,E,PS,PE] = PlayerId => PartialFunction[StateChange[S,E], presentation.EventAndState[PE, PS]]
  
}

class GamesStateChangeFilter(gameService: => BriscolaService, toPresentation: => GamePresentationAdapter) extends StateChangeFilter[GameState, BriscolaEvent, presentation.GameState, presentation.BriscolaEvent] {
  
  def apply(pid:PlayerId) = {
    case StateChange(_, ev, state @ ActiveGameState(id,_,_,_,_,_)) if state.players.map(_.id).contains(pid) && !gameService.isFinished(id) =>
      EventAndState(toPresentation(id, ev, pid), toPresentation(state, Some(pid)))
    case StateChange(_, ev, state @ FinalGameState(id,_,_,_)) if state.players.map(_.id).contains(pid)  =>
      EventAndState(toPresentation(id, ev, pid), toPresentation(state, Some(pid)))
  }
  
}

class PlayersStateChangeFilter(toPresentation: => PlayerPresentationAdapter) extends StateChangeFilter[Iterable[Player], PlayerEvent, Iterable[presentation.Player], presentation.PlayerEvent] {
  
  def apply(pid:PlayerId) = {
    case StateChange(_, e @ PlayerLogOff(id), s) if id != pid=> 
      EventAndState(toPresentation(e), toPresentation(s))
     case StateChange(_, e @ PlayerLogOn(_), s) => 
      EventAndState(toPresentation(e), toPresentation(s))
  }
  
}

class CompetitionsStateChangeFilter( competitionService: => CompetitionsService, toPresentation: => CompetitionPresentationAdapter) extends StateChangeFilter[CompetitionState, CompetitionEvent, presentation.CompetitionState, presentation.CompetitionEvent] {

  def nonSelfCompetitionEvent(event:CompetitionEvent, pid:PlayerId):Boolean = { 
    event match {
      case CreatedCompetition(_, pl, comp) => pl.id != pid
      case CompetitionAccepted(plid) => plid != pid
      case CompetitionDeclined(plid, _) => plid != pid
      case _ => true
    }
  }
  
  def isToSend(pid:PlayerId, e: ClientCompetitionEvent, s: ClientCompetitionState):Boolean =
    GamePlayers.getPlayers(s.competition.players).contains(pid) && nonSelfCompetitionEvent(e, pid) && !competitionService.isFullfilled(s.id) 
  
  def apply(pid:PlayerId) = {
    case StateChange(_, e: ClientCompetitionEvent, s: ClientCompetitionState) if isToSend(pid,e,s) => EventAndState(toPresentation(s.id, e, pid), toPresentation(s, Some(pid)))
  }
}
