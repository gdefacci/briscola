package org.obl.briscola
package web

import org.obl.ddd.StateChange
import org.obl.briscola.player.PlayerId
import presentation.EventAndState
import org.obl.briscola.service._
import org.obl.briscola.service.player.PlayerEvent

import player._
import competition._
import org.obl.briscola.player.Player
import org.obl.briscola.player.GamePlayers

object StateChangeFilter {

  type StateChangeFilter[S, E, PS, PE] = PlayerId => PartialFunction[StateChange[S, E], presentation.EventAndState[PE, PS]]

}

import StateChangeFilter._
import org.obl.briscola.web.util.PresentationAdapter

class GamesStateChangeFilter(gameService: => BriscolaService)(
    implicit playerGameEventPrsenetationAdapter: PresentationAdapter[PlayerGameEvent, presentation.BriscolaEvent],
    playerActiveGameStatePrsenetationAdapter: PresentationAdapter[PlayerActiveGameState, presentation.ActiveGameState],
    finalGameStatePrsenetationAdapter: PresentationAdapter[FinalGameState, presentation.FinalGameState]) extends StateChangeFilter[GameState, BriscolaEvent, presentation.GameState, presentation.BriscolaEvent] {

  def apply(pid: PlayerId) = {
    case StateChange(_, ev, state @ ActiveGameState(id, _, _, _, _, _)) if state.players.map(_.id).contains(pid) && !gameService.isFinished(id) =>
      EventAndState(PresentationAdapter(PlayerGameEvent(pid, id, ev)), PresentationAdapter(PlayerActiveGameState(pid, state)))
    case StateChange(_, ev, state @ FinalGameState(id, _, _, _)) if state.players.map(_.id).contains(pid) =>
      EventAndState(PresentationAdapter(PlayerGameEvent(pid, id, ev)), PresentationAdapter(state))
  }

}

class PlayersStateChangeFilter(implicit playerPresentationAdapter: PresentationAdapter[Player, presentation.Player], playerEventPresentationAdapter:PresentationAdapter[PlayerEvent, presentation.PlayerEvent] ) extends StateChangeFilter[Iterable[Player], PlayerEvent, Iterable[presentation.Player], presentation.PlayerEvent] {

  def apply(pid: PlayerId) = {
    case StateChange(_, e @ PlayerLogOff(id), s) if id != pid =>
      EventAndState(PresentationAdapter(e:PlayerEvent), s.map(p => PresentationAdapter(p)))
    case StateChange(_, e @ PlayerLogOn(id), s) if id != pid  =>
      EventAndState(PresentationAdapter(e:PlayerEvent), s.map(p => PresentationAdapter(p)))
  }

}

class CompetitionsStateChangeFilter(competitionService: => CompetitionsService)(
    implicit playerCompetitionEventPrsenetationAdapter: PresentationAdapter[PlayerCompetitionEvent, presentation.CompetitionEvent],
    playerCompetitionStatePrsenetationAdapter: PresentationAdapter[PlayerCompetitionState, presentation.CompetitionState]    
) extends StateChangeFilter[CompetitionState, CompetitionEvent, presentation.CompetitionState, presentation.CompetitionEvent] {

  def nonSelfCompetitionEvent(event: CompetitionEvent, pid: PlayerId): Boolean = {
    event match {
      case CreatedCompetition(_, pl, comp) => pl.id != pid
      case CompetitionAccepted(plid) => plid != pid
      case CompetitionDeclined(plid, _) => plid != pid
      case _ => true
    }
  }

  def isToSend(pid: PlayerId, e: ClientCompetitionEvent, s: ClientCompetitionState): Boolean =
    GamePlayers.getPlayers(s.competition.players).contains(pid) && nonSelfCompetitionEvent(e, pid) && !competitionService.isFullfilled(s.id)

  def apply(pid: PlayerId) = {
    case StateChange(_, e: ClientCompetitionEvent, s: ClientCompetitionState) if isToSend(pid, e, s) => 
      EventAndState(PresentationAdapter( PlayerCompetitionEvent(pid, s.id, e)), PresentationAdapter( PlayerCompetitionState(pid,s)))
  }
}
