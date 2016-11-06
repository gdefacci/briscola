package com.github.gdefacci.briscola.presentation.competition

import com.github.gdefacci.briscola.{ competition => model }
import com.github.gdefacci.briscola.player.GamePlayers
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.service.competition.CompetitionsService
import com.github.gdefacci.briscola.presentation.StateChangeFilter
import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.ddd.StateChange
import com.github.gdefacci.briscola.presentation.PlayerCompetitionEvent
import com.github.gdefacci.briscola.presentation.PlayerCompetitionState

class CompetitionsStateChangeFilter(competitionService: => CompetitionsService)(
    implicit playerCompetitionEventPresentationAdapter: PresentationAdapter[PlayerCompetitionEvent, CompetitionEvent],
    playerCompetitionStatePresentationAdapter: PresentationAdapter[PlayerCompetitionState, CompetitionState]    
) extends StateChangeFilter[model.CompetitionState, model.CompetitionEvent, CompetitionState, CompetitionEvent] {

  private def nonSelfCompetitionEvent(event: model.CompetitionEvent, pid: PlayerId): Boolean = {
    event match {
      case model.CreatedCompetition(_, pl, comp) => pl.id != pid
      case model.CompetitionAccepted(plid) => plid != pid
      case model.CompetitionDeclined(plid, _) => plid != pid
      case _ => true
    }
  }

  private def isToSend(pid: PlayerId, e: model.ClientCompetitionEvent, s: model.ClientCompetitionState): Boolean =
    GamePlayers.getPlayers(s.competition.players).contains(pid) && nonSelfCompetitionEvent(e, pid) && !competitionService.isFullfilled(s.id)

  def apply(pid: PlayerId) = {
    case StateChange(_, e: model.ClientCompetitionEvent, s: model.ClientCompetitionState) if isToSend(pid, e, s) => 
      EventAndState(PresentationAdapter( PlayerCompetitionEvent(pid, s.id, e)), PresentationAdapter( PlayerCompetitionState(pid,s)))
  }
}
