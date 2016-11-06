package com.github.gdefacci.briscola.presentation.player

import com.github.gdefacci.briscola.{ player => model }
import com.github.gdefacci.briscola.player.PlayerId
import com.github.gdefacci.briscola.presentation.EventAndState
import com.github.gdefacci.briscola.presentation.StateChangeFilter
import com.github.gdefacci.briscola.web.util.PresentationAdapter
import com.github.gdefacci.ddd.StateChange

class PlayersStateChangeFilter(
    implicit playerPresentationAdapter: PresentationAdapter[model.Player, Player], 
    playerEventPresentationAdapter:PresentationAdapter[model.PlayerEvent, PlayerEvent] ) 
    extends StateChangeFilter[Iterable[model.Player], model.PlayerEvent, Iterable[Player], PlayerEvent] {

  def apply(pid: PlayerId) = {
    case StateChange(_, e @ model.PlayerLogOff(id), s) if id != pid =>
      EventAndState(PresentationAdapter(e:model.PlayerEvent), s.map(p => PresentationAdapter(p)))
    case StateChange(_, e @ model.PlayerLogOn(id), s) if id != pid  =>
      EventAndState(PresentationAdapter(e:model.PlayerEvent), s.map(p => PresentationAdapter(p)))
  }

}
