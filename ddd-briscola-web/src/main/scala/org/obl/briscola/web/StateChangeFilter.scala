package org.obl.briscola
package web

import org.obl.ddd.StateChange
import argonaut.EncodeJson
import org.obl.briscola.player.PlayerId
import Presentation.EventAndState
import org.obl.briscola.service._
import org.obl.briscola.service.player.PlayerEvent
import org.obl.briscola.player.Player

object StateChangeFilter {
  
  type StateChangeFilter[S,E,PS,PE] = PlayerId => PartialFunction[StateChange[S,E], Presentation.EventAndState[PE, PS]]
  
}

import StateChangeFilter._
import player._
import competition._

class GamesStateChangeFilter(gameService: => BriscolaService, toPresentation: => GamePresentationAdapter) extends StateChangeFilter[GameState, BriscolaEvent, Presentation.GameState, Presentation.BriscolaEvent] {
  
  def apply(pid:PlayerId) = {
    case StateChange(_, ev, state @ ActiveGameState(id,_,_,_,_)) if !gameService.isFinished(id) =>
      EventAndState(toPresentation(id, ev, pid), toPresentation(state, Some(pid)))
    case StateChange(_, ev, state @ FinalGameState(id,_,_)) =>
      EventAndState(toPresentation(id, ev, pid), toPresentation(state, Some(pid)))
  }
  
}

class PlayersStateChangeFilter(toPresentation: => PlayerPresentationAdapter) extends StateChangeFilter[Iterable[Player], PlayerEvent, Iterable[Presentation.Player], Presentation.PlayerEvent] {
  
  def apply(pid:PlayerId) = {
    case StateChange(_, e @ PlayerLogOff(id), s) if id != pid=> 
      EventAndState(toPresentation(e), toPresentation(s))
     case StateChange(_, e @ PlayerLogOn(_), s) => 
      EventAndState(toPresentation(e), toPresentation(s))
  }
  
}

class CompetitionsStateChangeFilter( competitionService: => CompetitionsService, toPresentation: => CompetitionPresentationAdapter) extends StateChangeFilter[CompetitionState, CompetitionEvent, Presentation.CompetitionState, Presentation.CompetitionEvent] {

  def nonSelfCompetitionEvent(event:CompetitionEvent, pid:PlayerId):Boolean = { 
    event match {
      case CreatedCompetition(_, pl, comp) => pl.id != pid
      case CompetitionAccepted(plid) => plid != pid
      case CompetitionDeclined(plid, _) => plid != pid
      case _ => true
    }
  }
  
  def apply(pid:PlayerId) = {
    case StateChange(_, e: ClientCompetitionEvent, s: ClientCompetitionState) if nonSelfCompetitionEvent(e, pid) && !competitionService.isFullfilled(s.id) => 
      EventAndState(toPresentation(s.id, e, pid), toPresentation(s, Some(pid)))
  }
}