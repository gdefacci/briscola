package org.obl.briscola.presentation.adapters

import org.obl.briscola.web.{PlayerRoutes, PlayerWebSocketRoutes, CompetitionRoutes}
import org.obl.briscola.web.util.PresentationAdapter
import org.obl.briscola.presentation
import org.obl.briscola.service.player
import org.obl.briscola.player.{Player => DomainPlayer}

class PlayerPresentationAdapter(routes: PlayerRoutes,
  playerWebSocketRoutes: PlayerWebSocketRoutes,
  competitionRoutes: CompetitionRoutes) {
  

  implicit lazy val playerAdapter = PresentationAdapter((pl: DomainPlayer) =>
    presentation.Player(routes.PlayerById.encode(pl.id), pl.name,
      playerWebSocketRoutes.PlayerById.encode(pl.id),
      competitionRoutes.CreateCompetition.encode(pl.id)))
  
  implicit lazy val playerEventAdapter = PresentationAdapter[player.PlayerEvent, presentation.PlayerEvent]( (pe: player.PlayerEvent) => pe match {
    case player.PlayerLogOn(pid) => presentation.PlayerLogOn(routes.PlayerById.encode(pid))
    case player.PlayerLogOff(pid) => presentation.PlayerLogOff(routes.PlayerById.encode(pid))
  })
}

object PlayerPresentationAdapter {
  def apply(playerRoutes: PlayerRoutes, playerWebSocketRoutes: PlayerWebSocketRoutes, competitionRoutes: CompetitionRoutes) =
    new PlayerPresentationAdapter(playerRoutes, playerWebSocketRoutes, competitionRoutes)
    
}