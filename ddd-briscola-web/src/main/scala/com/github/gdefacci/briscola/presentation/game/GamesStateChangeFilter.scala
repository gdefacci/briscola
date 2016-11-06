package com.github.gdefacci.briscola.presentation.game

import com.github.gdefacci.briscola.{ game => model }
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.service.game.GameService
import com.github.gdefacci.briscola.presentation.StateChangeFilter
import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.ddd.StateChange
import com.github.gdefacci.briscola.presentation.PlayerGameEvent
import com.github.gdefacci.briscola.presentation.PlayerActiveGameState

class GamesStateChangeFilter(gameService: => GameService)(
    implicit playerGameEventPresentationAdapter: PresentationAdapter[PlayerGameEvent, BriscolaEvent],
    playerActiveGameStatePresentationAdapter: PresentationAdapter[PlayerActiveGameState, ActiveGameState],
    finalGameStatePreseneationAdapter: PresentationAdapter[model.FinalGameState, FinalGameState]) 
    extends StateChangeFilter[model.GameState, model.BriscolaEvent, GameState, BriscolaEvent] {

  def apply(pid: PlayerId) = {
    case StateChange(_, ev, state @ model.ActiveGameState(id, _, _, _, _, _)) if state.players.map(_.id).contains(pid) && !gameService.isFinished(id) =>
      EventAndState(PresentationAdapter(PlayerGameEvent(pid, id, ev)), PresentationAdapter(PlayerActiveGameState(pid, state)))
    case StateChange(_, ev, state @ model.FinalGameState(id, _, _, _)) if state.players.map(_.id).contains(pid) =>
      EventAndState(PresentationAdapter(PlayerGameEvent(pid, id, ev)), PresentationAdapter(state))
  }

}
